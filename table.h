#include "main.h"

typedef struct Table {
    struct Attribute* attributes[MAX_NUM_ATTRIBUTES];
    char name[MAX_NAME_SIZE];
    int recordCount;
    int currentPage;
} Table;

void initializeTable(Table* table);