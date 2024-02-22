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
    printf("name: %s\n", table->name);
    printf("num pages: %d\n", table->numPages);
    printf("num attributes: %d\n", table->numAttributes);
    printf("attributes:\n");
    for(int i = 0; i < table->numAttributes; i++) {
        displayAttribute(&(table->attributes[i]));

    }
}


bool hasPrimaryKey(TableSchema* table) { 
    for(int i = 0; i < table->numAttributes; i++) {
        if(table->attributes[i].primaryKey) {
            return true;
        }
    }
    return false;
}
