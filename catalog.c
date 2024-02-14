#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <cstring>

#include "catalog.h"


// Initialization Functions:

void initializeCatalog(Catalog* c) {
    c -> tables = NULL;
    c -> tableCount = 0;
    c -> bufferPool = NULL;
}

void initializeTable(Table* table) {
    for (int i = 0; i < MAX_NUM_ATTRIBUTES; ++i) {
        table->attributes[i] = NULL;
    }
    std:strcpy(table -> name, "");
    table -> recordCount = 0;
    table -> currentPage = 0;
}

// void initializeRecord(Record* record) {

// }

void initializeBufferPool(BufferPool* bufferPool) {
    //bufferPool -> pages = (struct Page*)malloc();
}

void initializePage(Page* page, int pageNumber) {

}

void initializeAttribute(Attribute* attribute) {

}


// Insertion Functions:

void addTable(Catalog* c, char name[MAX_NAME_SIZE]) {

}  // recordCount and currentPage initialized as 0

void addRecord(Catalog* c, int pageNumber, int id, char name[MAX_NAME_SIZE]) {

}                                                                              // Can we auto-initialize id to the next id instead of hard-coding it in?
                                                                               // Access the Page from the provided Catalog

void addPage(Catalog* c) {

}  // How do we deal with this one?

void addAttribute(Catalog* c, char name[MAX_NAME_SIZE], char type[20]) {

}  // Add more constraints as needed


// Search functions:

void findPage(BufferPool* bufferPool, int pageNumber) {

}  // Do we want to implement this to search for data instead?
