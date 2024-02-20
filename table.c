#include <stddef.h>
#include <string.h>
#include <stdlib.h>
#include "table.h"

void initializeTable(TableSchema* table, int numAttributes, char *name, AttributeSchema *attributes) {
    table->attributes = attributes;
    strcpy(table->name, name);
    table->numPages = 0;
    table->numAttributes = numAttributes;
}
