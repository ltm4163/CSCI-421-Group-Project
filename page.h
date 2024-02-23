#ifndef PAGE_H
#define PAGE_H
#include <stdbool.h>
#include "constants.h"
#include "record.h"

typedef struct Page {
    Record *records[MAX_NUM_RECORDS];
    void *data;
    int pageNumber;
    int tableNumber;
    int numRecords;
    int size;
    bool updated; //know whether page needs to be written to file
} Page;

void initializePage(Page* pg, int pageNumber, int tableNumber, bool updated);

#endif
