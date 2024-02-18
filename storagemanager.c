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

// Reads the desired page from hardware
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

Page* getPage(int tableNumber, int pageNumber) { //read desired page from hardware
    char file_dest[15];
    sprintf(file_dest, "tables/%d.bin", tableNumber);

    FILE *file = fopen(file_dest, "rb+");
    if (file == NULL) {
        printf("Error opening the file.\n");
        return NULL; // Return NULL to indicate failure
    }

    int address = (pageNumber-1) * MAX_PAGE_SIZE;
    char *page_buffer = (char *)malloc(MAX_PAGE_SIZE); // Allocate memory for the page buffer
    if (page_buffer == NULL) {
        printf("Memory allocation failed for page buffer.\n");
        fclose(file); // Make sure to close the file before returning
        return NULL;
    }

    fseek(file, address, SEEK_SET);
    fread(page_buffer, MAX_PAGE_SIZE, 1, file);
    fclose(file);

    Page *p = (Page*)malloc(sizeof(Page));
    if (p == NULL) {
        printf("Memory allocation failed for Page.\n");
        free(page_buffer); // Clean up previously allocated buffer
        return NULL;
    }
    initializePage(p, pageNumber, tableNumber);
    p->data = page_buffer; // Directly assign the allocated buffer
    // Ensure the data is null-terminated before treating it as a string
    page_buffer[MAX_PAGE_SIZE - 1] = '\0';
    printf("Text: %s\n", (char *)p->data); // Cast to char* for printing
    return p;
}

// retrieves catalog initiated in main.c
void initCatalog() { 
    catalog = getCatalog();
}

// retrieves buffer pool initiated in main.c
void initBuffer() { 
    bPool = getBufferPool();
}



// Record getRecord(int tableNumber, void* primaryKey) { //get record from table
//     Table table = catalog->tables[sizeof(Table)*tableNumber]; //assuming table numbers will start at 0 and not 1
//     Attribute pK; //primary key
//     for (int i = 0; i < MAX_NUM_ATTRIBUTES/sizeof(Attribute); i++) {
//         Attribute *attr = table.attributes[i*sizeof(Attribute)];
//         for (int j = 0; j < sizeof(attr->constraints)/sizeof(Constraint); j++) {

//         }
        
//     }
//     return;
