#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include "buffer.h"
#include "storagemanager.h"
#include "attribute.h"
#include "constraint.h"
#include "main.h"

Catalog *catalog;
Buffer *bPool;

Page *getRecords(int tableNumber) {
    Page *pages = (Page*)malloc(sizeof(Page)*1000); //1000 is placeholder value

}

//addRecord inserts a record to a certain table of a catalog
// void addRecord(Catalog* c, Record record, int tableNumber){
//     Page *pages=(Page *)malloc(sizeof(Page)*maxBufferSize); //unnecessary allocation?
//     if(c->tables[tableNumber].numPages==0){ //if no pages table
//             Page *page=(Page*)malloc(sizeof(Page));
//             initializePage(page, 0, tableNumber, true);
//             pages[0]=*page;
//             page->records[0] = &record;
//             bPool->pages = pages;
//             return NULL;
//     }

//     // if(bPool->pageCount==0){ //if no pages in buffer, check table file
    
//     // }
//     pages=bPool->pages;
//     int min_index;
    
//     for(int i = 0; i < bPool->pageCount; i++){ //iterate through pages in buffer
//         struct Page *pg=(Page *)malloc(sizeof(Page));
//         pg=&bPool->pages[i*sizeof(Page)];
//         if (pg->tableNumber != tableNumber) { //skip page if not in desired table
//             continue;
//         }
//         int size=sizeof(pg->records)/sizeof(*pg->records[0]);
//         for(int j = 0; j < size-1; j++){
//             min_index=j;
//             for(int k = j+1; k < size; k++){
//                 if(pg->records[k] < pg->records[min_index]){
//                     min_index=k;
                    
//                 }
                
//             }
//             struct Record *temprec=(struct Record *)malloc(sizeof(struct Record));
//             temprec=pg->records[min_index];
//             pg->records[min_index]=pg->records[j];
//             pg->records[j]=temprec;

//         }
//         inserted=true;
//         if(sizeof(pg->records)==MAX_NUM_RECORDS){
//             struct Page *newpg=(struct Page*)malloc(sizeof(struct Page));
//             splitpage(pg, newpg);
//         }
//     }
// }
    


// void splitpage(BufferPool*bp, Page *currentpg, Page *newpage){
//     int sizerecords=sizeof(currentpg->records)/(sizeof(currentpg->records[0]));
//     Record *firsthalf=(Record*)malloc(sizerecords/2 * sizeof(Record));
//     Record *secondhalf=(Record *)malloc(sizerecords/2 * sizeof(Record));
//     firsthalf=currentpg->records;
//     secondhalf=currentpg->records + sizerecords/2;
//     *currentpg->records=firsthalf;
//     *newpage->records=secondhalf;
//     int pos=0;
//     for(int b=0; b < sizeof(bp->pages)/sizeof(bp->pages[0]); b++){
//         if(currentpg == &bp->pages[b]){
//             pos=b;
//         }
//     }
//     int numberpgs=sizeof(bp->pages)/sizeof(bp->pages[0]);
//     for(int n=numberpgs-1; n >= pos; n--){
//         bp->pages[n]=bp->pages[n-1];
//     }
//     bp->pages[pos-1]=*newpage;
    

// }

// Convert page data into records using table schema
void createRecords(Page *page, int tableNumber) {
    TableSchema table = catalog->tables[tableNumber];
    int pageOffset = sizeof(int); //uses for knowing where next record starts in page
    for (int i = 0; i < page->numRecords; i++) { //iterates through all records in page
        Record *rec = (Record*)malloc(sizeof(Record));
        rec->data = (void*)malloc(MAX_PAGE_SIZE);
        int recordOffset = pageOffset; //used for knowing where next attribute starts in record
        for (int j = 0; j < table.numAttributes; j++) { //iterates through all attributes in table
            AttributeSchema *attr = &table.attributes[j];
            char *attrType = attr->type;
            int sizeToRead = attr->size; //used to tell fread how much data to read from page.data
            if (strcmp(attrType, "varchar") == 0) { //if type is varchar, read int that tells length of varchar
                memcpy(&sizeToRead, page->data+recordOffset, sizeof(int));
                memcpy(rec->data+recordOffset, &sizeToRead, sizeof(int));
                recordOffset += sizeof(int);
            }
            void *attrValue = (void*)malloc(sizeToRead); //value of attribute to be written to record struct
            memcpy(attrValue, page->data+recordOffset, sizeToRead);
            memcpy(rec->data+recordOffset, attrValue, sizeToRead); //write data to record.data
            
            recordOffset += sizeToRead;
        }
        rec->data = (void*)realloc(rec->data, recordOffset-pageOffset); //cut size of record.data down to size of data stored
        rec->size = recordOffset-pageOffset;
        pageOffset += recordOffset;
        page->records[i] = rec;
    }
}

// Read desired page from hardware
Page* getPage(int tableNumber, int pageNumber) { 
    char file_dest[15];
    sprintf(file_dest, "tables/%d.bin", tableNumber); //set file_dest variable to name of table file
    FILE *file = fopen(file_dest, "rb+");
    if (file == NULL) {
        printf("Error opening the file.\n");
        return NULL; // NULL indicates failure!!!
    }

    int address = (pageNumber * MAX_PAGE_SIZE)+4; //location of page in file (skip numPages)
    void *page_buffer = (void *)malloc(MAX_PAGE_SIZE);
    if (page_buffer == NULL) {
        printf("Memory allocation failed for page buffer.\n");
        fclose(file); // Closes file
        return NULL;
    }

    fseek(file, address, SEEK_SET); //start reading from location of page in file
    fread(page_buffer, MAX_PAGE_SIZE, 1, file);
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
    memcpy(&numRecords, page_buffer, sizeof(int));
    p->numRecords = numRecords;
    createRecords(p, tableNumber);
    return p;
}

//look for page in buffer pool
Page *findPage(int tableNumber, int pageNumber) {
    Page *page = (Page*)malloc(sizeof(Page));
    for (int j = 0; j < buf_size(bPool); j++) {
        if (buf_get(bPool, page) == -1) {
            printf("No pages in buffer\n");
            return NULL;
        }
        if (page->tableNumber == tableNumber && page->pageNumber == pageNumber) {
            return page;
        }
    }
    return page;
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
