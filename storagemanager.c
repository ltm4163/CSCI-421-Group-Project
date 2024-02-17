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
    //printf("dest: %s\n", file_dest);
    FILE *file = fopen(file_dest, "rb+");
    //printf("File name: %d.bin\n", tableNumber);

    if (file == NULL) {
        printf("Error opening the file.\n");
        return;
    }

    int address = (pageNumber-1)*MAX_PAGE_SIZE;
    char page_buffer[MAX_PAGE_SIZE];
    fseek(file, address, SEEK_SET);
    fread(page_buffer, MAX_PAGE_SIZE, 1, file);
    fclose(file);

    Page *p = (Page*)malloc(sizeof(Page));
    if (p == NULL) {
        printf("Memory allocation failed.\n");
        return;
    }
    initializePage(p, pageNumber, tableNumber);
    p->data = (void*)malloc(MAX_PAGE_SIZE);
    strcpy(p->data, page_buffer);
    printf("Text: %s\n", p->data);
    return p;
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
// }

void initCatalog() { //retreives catalog initiated in main.c
    catalog = getCatalog();
}

void initBuffer() { //retreives buffer pool initiated in main.c
    bPool = getBufferPool();
}