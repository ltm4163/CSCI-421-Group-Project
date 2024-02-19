#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include "bufferpool.h"
#include "storagemanager.h"
#include "attribute.h"
#include "constraint.h"
#include "main.h"

Catalog *catalog;
BufferPool *bPool;

//addRecord inserts a record to a certain table of a catalog
void addRecord(Catalog* c, Record record, int tableNumber){
    Page *pages=(Page *)malloc(sizeof(Page)*maxBufferSize); //unnecessary allocation?
    if(bPool->pageCount==0){ //if no pages in buffer, check table file
        char file_dest[15];
        sprintf(file_dest, "tables/%d.bin", tableNumber); //set file_dest variable to name of table file
        //printf("dest: %s\n", file_dest);
        FILE *file = fopen(file_dest, "rb+");
        if (file == NULL) { //if file doesn't exist, create new page and add to buffer (maybe create file?)
            Page *page=(Page*)malloc(sizeof(Page));
            initializePage(page, 0, tableNumber, true);
            pages[0]=*page;
            page->records[0] = &record;
            bPool->pages = pages;
            return NULL;
        }

        //implementation for if file exists and has pages:
    }
    
    pages=bPool->pages;
    int min_index;
    bool inserted = false;
    for(int i = 0; i < bPool->pageCount; i++){ //iterate through pages in buffer
        struct Page *pg=(Page *)malloc(sizeof(Page));
        pg=&bPool->pages[i*sizeof(Page)];
        if (pg->tableNumber != tableNumber) { //skip page if not in desired table
            continue;
        }
        int size=sizeof(pg->records)/sizeof(pg->records[0]);
        for(int j = 0; j < size-1; j++){
            min_index=j;
            for(int k = j+1; k < size; k++){
                if(pg->records[k] < pg->records[min_index]){
                    min_index=k;
                }
            }
            struct Record *temprec=(struct Record *)malloc(sizeof(struct Record));
            temprec=pg->records[min_index];
            pg->records[min_index]=pg->records[j];
            pg->records[j]=temprec;

        }
        inserted=TRUE;
        if(sizeof(pg->records)==MAX_NUM_RECORDS){
            struct Page *newpg=(struct Page*)malloc(sizeof(struct Page));
            splitpage(pg, newpage);
        }
    }
}

// Convert page data into records using table schema
void createRecords(Page *page, int tableNumber) {
    TableSchema table = catalog->tables[tableNumber];
    for (int i = 0; i < page->numRecords; i++) { //iterates through all pages in buffer
        Record *rec = (Record*)malloc(sizeof(Record));
        for (int j = 0; j < sizeof(table.attributes)/sizeof(AttributeSchema); j++) { //iterates through all attributes in table
            AttributeSchema *attr = table.attributes[j*sizeof(AttributeSchema)];
            char *attrType = attr->type;
            void *attrValue; //value of attribute to be written to record struct
            int sizeToRead = attr->size; //used to tell fread how much data to read from page.data
            if (strcmp(attrType, "varchar") == 0) { //if type is varchar, read int that tells length of varchar
                fread(&sizeToRead, 4, 1, page->data);
                fwrite(&sizeToRead, 4, 1, rec->data);
            }
            fread(attrValue, sizeToRead, 1, page->data);
            fwrite(attrValue, sizeToRead, 1, rec->data); //write data to record.data
        }
        page->records[i] = rec;
    }
}

// Read desired page from hardware
Page* getPage(int tableNumber, int pageNumber) { 
    char file_dest[15];
    sprintf(file_dest, "tables/%d.bin", tableNumber); //set file_dest variable to name of table file
    //printf("dest: %s\n", file_dest);
    FILE *file = fopen(file_dest, "rb+");
    if (file == NULL) {
        printf("Error opening the file.\n");
        return NULL; // NULL indicates failure!!!
    }

    int address = (pageNumber) * MAX_PAGE_SIZE; //location of page in file
    char *page_buffer = (char *)malloc(MAX_PAGE_SIZE);
    if (page_buffer == NULL) {
        printf("Memory allocation failed for page buffer.\n");
        fclose(file); // Closes file
        return NULL;
    }

    fseek(file, address+4, SEEK_SET); //start reading from location of page in file (skip numPages)
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
    fread(&numRecords, 4, 1, page_buffer);
    p->numRecords = numRecords;

    // Ensure the data is null-terminated before treating it as a string
    page_buffer[MAX_PAGE_SIZE - 1] = '\0';
    printf("Text: %s\n", (char *)p->data); // Casts to char* for printing
    return p;
}

//look for page in buffer pool
Page findPage(int tableNumber, int pageNumber) {
    Page page;
    for (int j = 0; j < bPool -> pageCount; j++) {
        page = bPool -> pages[j*sizeof(Page)];
        if (page.tableNumber == tableNumber && page.pageNumber == pageNumber) {
            return page;
        }
    }
    return page;
}

//get record from table
Record getRecord(int tableNumber, void* primaryKey) {
    TableSchema table = catalog->tables[tableNumber*sizeof(TableSchema)]; //assuming table numbers will start at 0 and not 1
    AttributeSchema pK; //primary key
    for (int i = 0; i < MAX_NUM_ATTRIBUTES/sizeof(AttributeSchema); i++) { //set pK to primary key attribute of table
        AttributeSchema *attr = table.attributes[i*sizeof(AttributeSchema)];
        if (attr->primarKey == true)
        {
            break;
        }
    }
    for (int i = 0; i < table.numPages; i++) { //find ith page in table
        Page page;
        page = findPage(tableNumber, i); //look for page in buffer
        if (&page == NULL) { //read in page i of table from hardware if not in buffer pool
            page = *getPage(tableNumber, i);
        }
    }
    
    return;
}

// Retrieves catalog initiated in main.c
void initCatalog() { //retreives catalog initiated in main.c
    catalog = getCatalog();
}

// Retrieves buffer pool initiated in main.c
void initBuffer() {
    bPool = getBufferPool();
}

//retrieve buffer and catalog
void initializeStorageManager() {
    initCatalog();
    initBuffer();
}