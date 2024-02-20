#include <stddef.h>
#include <string.h>
#include "table.h"

void initializeTable(TableSchema* table) {
    for (int i = 0; i < MAX_NUM_ATTRIBUTES; ++i) {
        table->attributes[i] = NULL;
    }
    strcpy(table->name, "");
    table->numPages = 0;
}
