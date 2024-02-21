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
    for(int i = 0; i < table->numAttributes; i++) {
        displayAttribute(&(table->attributes[i]));

    }
    printf("num pages: %s", table->numPages);
    printf("num records: %s", table->numAttributes);
}
