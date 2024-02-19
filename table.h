#ifndef TABLE_H
#define TABLE_H
#include "constants.h"

typedef struct TableSchema {
    struct Attribute* attributes[MAX_NUM_ATTRIBUTES];
    char name[MAX_NAME_SIZE];
    int tableNumber;
    int numPages;
} TableSchema;

void initializeTable(TableSchema* tab);

#endif
