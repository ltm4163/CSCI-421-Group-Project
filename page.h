#ifndef PAGE_H
#define PAGE_H
#include "constants.h"
#include "record.h"

typedef struct Page {
    struct Record records[MAX_NUM_RECORDS];
    void *data;
    int pageNumber;
    int tableNumber;
} Page;

void initializePage(Page* pg, int pageNumber, int tableNumber);

#endif
