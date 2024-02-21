#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "catalog.h"


// Initialization Functions:

void initializeCatalog(Catalog* c, int count) {
    c -> tables = (TableSchema*)malloc(sizeof(TableSchema));
    c->tableCount = count;
}


// Insertion Functions:

void addTable(Catalog* c, TableSchema* table) {
    TableSchema* newTables = realloc(c->tables, (c->tableCount + 1) * sizeof(TableSchema));
    if(newTables != NULL) {
        newTables[c->tableCount] = *table;
        c->tables = newTables;
        c->tableCount++;
    } else { fprintf(stderr, "Memory allocation failed\n"); }

}  // recordCount and currentPage initialized as 0

void dropTable(Catalog* c, char name[MAX_NAME_SIZE]) {
    
}

void addPage(Catalog* c) {

}  // How do we deal with this one?

void addAttribute(Catalog* c, char name[MAX_NAME_SIZE], char type[20]) {

}  // Add more constraints as needed

void displayCatalog(Catalog* c) {
    for(int i = 0; i < c->tableCount; i++) {
        displayTable(&(c->tables[i]));
    }
}
