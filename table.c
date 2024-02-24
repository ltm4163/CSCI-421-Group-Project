#include <stddef.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include "table.h"

void initializeTable(TableSchema* table, int numAttributes, char *name, AttributeSchema *attributes) {
    table->attributes = attributes;
    strcpy(table->name, name);
    table->numPages = 0;
    table->numAttributes = numAttributes;
}

void displayTable(TableSchema* table) {
    printf("Table name: %s\n", table->name);
    printf("attributes:\n");
    for(int i = 0; i < table->numAttributes; i++) {
        printf("\t");
        displayAttribute(&(table->attributes[i]));

    }
    printf("Pages: %d\n", table->numPages);
}


bool hasPrimaryKey(TableSchema* table) { 
    for(int i = 0; i < table->numAttributes; i++) {
        if(table->attributes[i].primaryKey) {
            return true;
        }
    }
    return false;
}
