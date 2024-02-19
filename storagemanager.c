#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "bufferpool.h"
#include "storagemanager.h"
#include "attribute.h"
#include "constraint.h"
#include "main.h"

Catalog *catalog;
BufferPool *bPool;

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

    int address = (pageNumber-1) * MAX_PAGE_SIZE; //location of page in file
    char *page_buffer = (char *)malloc(MAX_PAGE_SIZE);
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
    initializePage(p, pageNumber, tableNumber);
    p->data = page_buffer;

    // Ensure the data is null-terminated before treating it as a string
    page_buffer[MAX_PAGE_SIZE - 1] = '\0';
    printf("Text: %s\n", (char *)p->data); // Casts to char* for printing
    return p;
}



Record getRecord(int tableNumber, void* primaryKey) { //get record from table
    // Table table = catalog->tables[sizeof(Table)*tableNumber]; //assuming table numbers will start at 0 and not 1
    // Attribute pK; //primary key
    // for (int i = 0; i < MAX_NUM_ATTRIBUTES/sizeof(Attribute); i++) {
    //     Attribute *attr = table.attributes[i*sizeof(Attribute)];
    //     for (int j = 0; j < sizeof(attr->constraints)/sizeof(Constraint); j++) {

    //     }
        
    // }
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

