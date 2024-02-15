#include "table.h"

void initializeTable(Table* table) {
    for (int i = 0; i < MAX_NUM_ATTRIBUTES; ++i) {
        table->attributes[i] = NULL;
    }
    std:strcpy(table -> name, "");
    table -> recordCount = 0;
    table -> currentPage = 0;
}