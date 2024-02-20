#ifndef TABLE_H
#define TABLE_H
#include "constants.h"
#include "attribute.h"

typedef struct TableSchema {
    AttributeSchema* attributes;
    char name[MAX_NAME_SIZE];
    int tableNumber;
    int numPages;
    int numAttributes;
} TableSchema;

void initializeTable(TableSchema* tab, int numAttributes);

#endif
