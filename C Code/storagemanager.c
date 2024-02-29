#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include "storagemanager.h"
#include "attribute.h"
#include "constraint.h"
#include "main.h"

Catalog *catalog;
Buffer *bPool;

// implements select *
void getRecords(int tableNumber) {
    TableSchema table = catalog->tables[tableNumber];
    AttributeSchema attr = table.attributes[0];
    Page *pages = (Page*)malloc(sizeof(Page)*catalog->tables[tableNumber].numPages); //locally store pages
    bool *pagesInBuf = (bool*)malloc(sizeof(bool)*table.numPages); //keep track of which pages are in buffer
    memset(pagesInBuf, false, table.numPages); //initialize pagesInBuf values to false
    
    int poolSize = buf_size(bPool);
    for(int i = 0; i < poolSize; i++){ //find table's pages in buffer
        Page *pg=(Page *)malloc(sizeof(Page));
        buf_get(bPool, pg);
        if (pg->tableNumber != tableNumber) { //skip page if not in desired table
            buf_putr(bPool, *pg);
            continue;
        }
        pages[pg->pageNumber] = *pg;
        pagesInBuf[pg->pageNumber] = true; //if page in buffer, set corresponding value in pagesInBuf to true
    }

    // print attribute names
    char *attrBorder = (char*)malloc((table.numAttributes*7)+1);
    for (int i = 0; i < table.numAttributes; i++)
    {
        strcpy(attrBorder+(i*7), "-------");
    }
    strcpy(attrBorder+(table.numAttributes*7), "\0");
    printf("%s\n", attrBorder);
    printf("|");
    for (int i = 0; i < table.numAttributes; i++)
    {
        printf(" %s |", table.attributes[i].name);
    }
    printf("\n%s\n", attrBorder);
    free(attrBorder);
    

    for (int i = 0; i < table.numPages; i++) { //get remaining pages from file
        if (pagesInBuf[i] == false) {
            struct Page *pg=(Page *)malloc(sizeof(Page));
            pg = getPage(tableNumber, i);
            pages[i] = *pg;
        }
        Page page = pages[i];
        for (int j = 0; j < page.numRecords; j++) { //iterates through all records in page
            Record *rec = page.records[j];
            int recordOffset = 0; //used for knowing where next attribute starts in record
            printf("|");
            for (int k = 0; k < table.numAttributes; k++) { //iterates through all attributes in table
                AttributeSchema *attr = &table.attributes[k];
                char *attrType = attr->type;
                int sizeToRead = attr->size; //used to tell how much data to read from page.data
                if (strcmp(attrType, "varchar") == 0) { //if type is varchar, read int that tells length of varchar
                    memcpy(&sizeToRead, rec->data+recordOffset, sizeof(int));
                    recordOffset += sizeof(int);
                }
                void *attrValue = (void*)malloc(sizeToRead); //value of attribute to be written to record struct
                memcpy(attrValue, rec->data+recordOffset, sizeToRead);

                // print record values
                if (strcmp(attr->type, "integer") == 0)
                {
                    int *value = (int*)attrValue;
                    printf(" %d|", *value);
                }
                else if (strcmp(attr->type, "double") == 0)
                {
                    double *value = (double*)attrValue;
                    printf(" %f|", *value);
                }
                else if (strcmp(attr->type, "boolean") == 0)
                {
                    bool *flag = (bool*)attrValue;
                    printf("%s|", *flag?"true":"false");
                }
                else if (strcmp(attr->type, "char") == 0 || strcmp(attr->type, "varchar") == 0)
                {
                    printf(" %s|", (char*)(attrValue));
                }
                
                recordOffset += sizeToRead;
            }
            printf("\n");
        }
        buf_putr(bPool, page);
    }
}

// compare primary key of two records to find where to insert record
bool *compare(AttributeSchema *attr, Record *insertRecord, Record *existingRecord, int *recordOffset, int *insertOffset) {
    char *attrType = attr->type;
    int sizeToRead = attr->size; //used to tell how much data to read from existingRecord.data
    int insertSize = attr->size; //used to tell how much data to read from insertRecord.data

    if (strcmp(attrType, "varchar") == 0) { //if type is varchar, read int that tells length of varchar
        memcpy(&sizeToRead, existingRecord->data+*recordOffset, sizeof(int));
        *recordOffset += sizeof(int)+sizeToRead;
        memcpy(&insertSize, insertRecord->data+*insertOffset, sizeof(int));
        *insertOffset += sizeof(int)+insertSize;
    }
    void *recValue = (void*)malloc(sizeToRead); //value of attribute to be written to record struct
    memcpy(recValue, existingRecord->data+*recordOffset, sizeToRead);
    void *insertValue = (void*)malloc(sizeToRead); //value of attribute to be written to record struct
    memcpy(insertValue, insertRecord->data+*insertOffset, insertSize);
    
    *recordOffset += sizeToRead;
    *insertOffset += insertSize;

    // compare values to find insert location
    bool *result = (bool*)malloc(sizeof(bool));
    if (strcmp(attr->type, "integer") == 0)
    {
        if (*((int*)insertValue) == *((int*)recValue))
        {
            result = NULL;
            return result;
        }
        *result = *((int*)insertValue) < *((int*)recValue);
        return result;
    }
    else if (strcmp(attr->type, "double") == 0)
    {
        if (*((double*)insertValue) == *((double*)recValue))
        {
            result = NULL;
            return result;
        }
        *result = *((double*)insertValue) < *((double*)recValue);
        return result;
    }
    else if (strcmp(attr->type, "boolean") == 0)
    {
        if (*((bool*)insertValue) == *((bool*)recValue))
        {
            result = NULL;
            return result;
        }
        *result = *((bool*)insertValue) < *((bool*)recValue);
        return result;
    }
    else if (strcmp(attr->type, "char") == 0 || strcmp(attr->type, "varchar") == 0) {
        int stringResult = strcmp((char*)insertValue, (char*)recValue);
        if (stringResult < 0)
        {
            *result = true;
            return result;
        }
        else if (stringResult > 0)
        {
            *result = false;
            return result;
        }
        else {
            result = NULL;
            return result;
        }
    }
}

// split a page into two when overfull
void splitpage(Buffer *bp, Page *currentpg, Page *newpage){
    int numRecords=currentpg->numRecords;
    int splitPoint = numRecords/2;
    Record **firsthalf=(Record**)malloc(100 * sizeof(Record*));
    Record **secondhalf=(Record**)malloc(100 * sizeof(Record*));
    for (int i = 0; i < splitPoint; i++) firsthalf[i] = currentpg->records[i];
    for (int i = splitPoint; i < numRecords; i++) secondhalf[i-splitPoint] = currentpg->records[i];
    currentpg->records=firsthalf;
    newpage->records=secondhalf;

    // update page sizes
    newpage->numRecords = numRecords-splitPoint;
    currentpg->numRecords = splitPoint;
    int currUpdatedSize = 0;
    for (int i = 0; i < splitPoint; i++)  currUpdatedSize += currentpg->records[i]->size;
    newpage->size = currentpg->size-currUpdatedSize;
    currentpg->size = currUpdatedSize+sizeof(int); //accounts for numRecs int

    buf_putr(bp, *currentpg);
    buf_putr(bp, *newpage);
}

//addRecord inserts a record to a certain table of a catalog
void addRecord(Catalog* c, Record *record, int tableNumber){
    Page *pages=(Page *)malloc(sizeof(Page)*maxBufferSize); //local storage of pages in table
    TableSchema *table = &c->tables[tableNumber];
    if(table->numPages==0){ //if no pages in table, create new page
            Page *page=(Page*)malloc(sizeof(Page));
            initializePage(page, 0, tableNumber, true);
            pages[0]=*page;
            page->records[0] = record;
            page->numRecords = 1;
            buf_put(bPool, *page);
            table->numPages++;
            table->pageLocations = (int*)malloc(1);
            table->pageLocations[0] = 0;
            return;
    }

    bool *pagesInBuf = (bool*)malloc(sizeof(bool)*table->numPages); //keep track of which pages are in buffer
    memset(pagesInBuf, false, table->numPages); //initialize pagesInBuf values to false
    
    for(int i = 0; i < buf_size(bPool); i++){ //find table's pages in buffer
        struct Page *pg=(Page *)malloc(sizeof(Page));
        if (buf_get(bPool, pg) == -1) break;
        if (pg->tableNumber != tableNumber) { //skip page if not in desired table
            continue;
        }

        //if page in buffer, set corresponding value in pagesInBuf to true and add to local pages storage
        pages[pg->pageNumber] = *pg;
        pagesInBuf[pg->pageNumber] = true;
    }

    bool maintainConstraints = false; // if an attribute has unique or notNull, search entire table
    bool indexFound = false; // flag for if insert location found
    // used for location to insert record in table
    int pageIndex;
    int recIndex;

    for (int i = 0; i < table->numPages; i++) { //iterate through all pages to find insert location
        if (indexFound && !maintainConstraints) break;

        // if page wasn't in buffer, add to local pages storage
        if (pagesInBuf[i] == false) {
            struct Page *pg=(Page *)malloc(sizeof(Page));
            pg = getPage(tableNumber, i);
            pages[i] = *pg;
            // TODO: also add page to buffer
        }

        Page page = pages[i];
        for (int j = 0; j < page.numRecords; j++) { //iterates through all records in page
            if (indexFound && !maintainConstraints) break;
            
            Record *rec = page.records[j];
            int *recordOffset = (int*)malloc(sizeof(int)); //location in existing record.data
            *recordOffset = 0;
            int *insertOffset = (int*)malloc(sizeof(int)); //location in insert record.data
            *insertOffset = 0;
            for (int k = 0; k < table->numAttributes; k++) { //iterates through all attributes in table
                AttributeSchema *attr = &table->attributes[k];
                if (attr->nonNull == true || attr->unique == true) maintainConstraints = true;
                if (attr->primaryKey == true && !indexFound) {
                    bool *result = compare(attr, record, rec, recordOffset, insertOffset);
                    if (result == NULL) {
                        //TODO: cancel insert error handling
                        printf("cancel!"); //placeholder
                        return;
                    }
                    else if (result) {
                        indexFound = true;
                        pageIndex = i;
                        recIndex = j;
                    }
                    
                }
                else {
                    if (attr->unique == true) {
                        bool *result = compare(attr, record, rec, recordOffset, insertOffset);
                        if (result == NULL) {
                            //TODO: cancel insert error handling
                            printf("cancel!"); //placeholder
                            return;
                        }
                    }
                    if (attr->nonNull == true)
                    {
                        //TODO: check if value equals agreed upon NULL placeholder
                        //  cancel insert if so
                    }
                    
                    
                    if (strcmp(attr->type, "varchar") == 0) { //if type is varchar, read int that tells length of varchar
                        int sizeToRead;
                        int insertSize;
                        memcpy(&sizeToRead, rec->data+*recordOffset, sizeof(int));
                        *recordOffset += sizeof(int)+sizeToRead;
                        memcpy(&insertSize, record->data+*insertOffset, sizeof(int));
                        *insertOffset += sizeof(int)+insertSize;
                    }
                    else {
                        *recordOffset += attr->size;
                        *insertOffset += attr->size;
                    }
                }

            }
        }
    }

    // insert record
    Page page;
    if (indexFound) {
        page = pages[pageIndex];
        // move records after insertion point down by 1
        for (int i = page.numRecords; i > recIndex; i--) page.records[i] = page.records[i-1];
    }
    else {
        pageIndex = table->numPages-1;
        page = pages[pageIndex];
        recIndex = page.numRecords;
    }
    page.records[recIndex] = record;
    page.numRecords++;
    page.size += record->size;
    page.updated = true;
    if (page.size > maxPageSize) { //split page if overfull
        Page *newPage = (Page*)malloc(sizeof(Page));
        initializePage(newPage, page.pageNumber+1, page.tableNumber, true);
        splitpage(bPool, &page, newPage);
        table->numPages++;
        
        // update page locations in table (include new page, move later pages down)
        table->pageLocations = (int*)realloc(table->pageLocations, table->numPages);
        for (int i = table->numPages-1; i > newPage->pageNumber; i--)
        {
            table->pageLocations[i] = table->pageLocations[i-1];
        }
        table->pageLocations[newPage->pageNumber] = table->numPages-1;

        // update pageNumbers of pages in buffer that were moved in pageLocations array
        for (int i = 0; i < buf_size(bPool); i++) {
            struct Page *pg=(Page *)malloc(sizeof(Page));
            if (buf_get(bPool, pg) == -1) break;
            if (pg->tableNumber = tableNumber && pg->pageNumber >= newPage->pageNumber)
            {
                pg->pageNumber++;
            }
            buf_putr(bPool, *pg);
        }
        
    }
    else buf_putr(bPool, page);
}

// Convert page data into records using table schema
void createRecords(Page *page, int tableNumber) {
    TableSchema table = catalog->tables[tableNumber];
    int pageOffset = sizeof(int); //uses for knowing where to read from page
    for (int i = 0; i < page->numRecords; i++) { //iterates through all records in page
        Record *rec = (Record*)malloc(sizeof(Record));
        rec->data = (void*)malloc(maxPageSize);
        int recordOffset = 0; //used for knowing where to write to record
        for (int j = 0; j < table.numAttributes; j++) { //iterates through all attributes in table
            AttributeSchema *attr = &table.attributes[j];
            char *attrType = attr->type;
            int sizeToRead = attr->size; //used to tell fread how much data to read from page.data
            if (strcmp(attrType, "varchar") == 0) { //if type is varchar, read int that tells length of varchar
                memcpy(&sizeToRead, page->data+pageOffset, sizeof(int));
                memcpy(rec->data+recordOffset, &sizeToRead, sizeof(int));
                recordOffset += sizeof(int);
                pageOffset += sizeof(int);
            }
            void *attrValue = (void*)malloc(sizeToRead); //value of attribute to be written to record struct
            memcpy(attrValue, page->data+pageOffset, sizeToRead);
            memcpy(rec->data+recordOffset, attrValue, sizeToRead); //write data to record.data
            
            
            recordOffset += sizeToRead;
            pageOffset += sizeToRead;
        }
        rec->data = (void*)realloc(rec->data, recordOffset); //cut size of record.data down to size of data stored
        rec->size = recordOffset;
        page->records[i] = rec;
        page->size += recordOffset;
    }
}

// Read desired page from hardware
Page* getPage(int tableNumber, int pageNumber) { 
    char file_dest[256];
    sprintf(file_dest, "%s/tables/%d.bin",getDbDirectory(), tableNumber); //set file_dest variable to name of table file
    FILE *file = fopen(file_dest, "rb+");
    if (file == NULL) {
        printf("Error opening the file.\n");
        return NULL; // NULL indicates failure!!!
    }

    int address = (catalog->tables[tableNumber].pageLocations[pageNumber] * maxPageSize)+sizeof(int); //location of page in file (skip numPages)
    void *page_buffer = (void *)malloc(maxPageSize);
    if (page_buffer == NULL) {
        printf("Memory allocation failed for page buffer.\n");
        fclose(file); // Closes file
        return NULL;
    }

    fseek(file, address, SEEK_SET); //start reading from location of page in file
    fread(page_buffer, maxPageSize, 1, file);
    fclose(file);

    Page *p = (Page*)malloc(sizeof(Page));
    if (p == NULL) {
        printf("Memory allocation failed for Page.\n");
        free(page_buffer);
        return NULL;
    }
    initializePage(p, pageNumber, tableNumber, false);
    p->data = page_buffer; //set data to data read from file
    int numRecords;
    memcpy(&numRecords, p->data, sizeof(int));
    p->numRecords = numRecords;
    createRecords(p, tableNumber);
    return p;
}

//look for page in buffer pool
Page *findPage(int tableNumber, int pageNumber) {
    Page *page = (Page*)malloc(sizeof(Page));
    for (int j = 0; j < buf_size(bPool); j++) {
        buf_get(bPool, page);
        if (page->tableNumber == tableNumber && page->pageNumber == pageNumber) {
            return page;
        }
    }
    return page;
}

void dropTableStorageManager(Catalog* c, const char* name) {
    int tableIndex = -1;

    // Find the table index
    for (int i = 0; i < c->tableCount; i++) {
        if (strcmp(c->tables[i].name, name) == 0) {
            tableIndex = i;
            break;
        }
    }

    if (tableIndex != -1) {
        // Optionally, clear related pages from the buffer before dropping the table
        clearTablePagesFromBuffer(bPool, c->tables[tableIndex].tableNumber);

        // Shift the remaining tables in the catalog to fill the gap
        for (int i = tableIndex; i < c->tableCount - 1; i++) {
            c->tables[i] = c->tables[i + 1];
        }
        c->tableCount--;

        // Reallocate the catalog tables to the new size
        if (c->tableCount > 0) {
            TableSchema* newTables = realloc(c->tables, sizeof(TableSchema) * c->tableCount);
            if (newTables != NULL) {
                c->tables = newTables;
            } else {
                // Handle reallocation error
            }
        } else {
            // If no tables are left, free the catalog tables array
            free(c->tables);
            c->tables = NULL;
        }

        printf("Table '%s' dropped successfully.\n", name);

        // Remove the table's data file from the filesystem
        char filepath[256];
        snprintf(filepath, sizeof(filepath), "tables/%s.bin", name);
        if (remove(filepath) == 0) {
            printf("File '%s' successfully deleted.\n", filepath);
        } else {
            perror("Failed to delete file");
        }
    } else {
        printf("Table '%s' not found.\n", name);
    }
}

// Example function to free dynamically allocated contents of a TableSchema
void freeTableSchemaContents(TableSchema* table) {
    // If you have dynamically allocated elements like attributes, free them here
    if(table->attributes != NULL) {
        free(table->attributes);  // Assuming 'attributes' is dynamically allocated
        table->attributes = NULL;
    }
    // Add more cleanup as necessary
}


//get record from table
//Record getRecord(int tableNumber, void* primaryKey) {
//    TableSchema table = catalog->tables[tableNumber*sizeof(TableSchema)]; //assuming table numbers will start at 0 and not 1
//    AttributeSchema pK; //primary key
//    for (int i = 0; i < MAX_NUM_ATTRIBUTES/sizeof(AttributeSchema); i++) { //set pK to primary key attribute of table
//        AttributeSchema *attr = &table.attributes[i*sizeof(AttributeSchema)];
//        if (attr->primaryKey == true)
//        {
//            break;
//        }
//    }
//    for (int i = 0; i < table.numPages; i++) { //find ith page in table
//        Page page;
//        page = findPage(tableNumber, i); //look for page in buffer
//        if (&page == NULL) { //read in page i of table from hardware if not in buffer pool
//            page = *getPage(tableNumber, i);
//        }
//   }
//    
//    return;
//}

// Retrieves catalog initiated in main.c
void initCatalog() { //retreives catalog initiated in main.c
    catalog = getCatalog();
}

// Retrieves buffer pool initiated in main.c
void initBuffer() {
    bPool = getBuffer();
}

//retrieve buffer and catalog (call early in main)
void initializeStorageManager() {
    initCatalog();
    initBuffer();
}
