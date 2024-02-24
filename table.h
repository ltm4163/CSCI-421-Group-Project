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
    int *pageLocations; //used for storing pages in file (index=pageNumber; value=location in file)
} TableSchema;

void initializeTable(TableSchema* table, int numAttributes, char *name, AttributeSchema *attributes);

void displayTable(TableSchema* table);

bool hasPrimaryKey(TableSchema* table);

#endif
