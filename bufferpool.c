#include <stdlib.h>
#include "bufferpool.h"

void initializeBufferPool(BufferPool* bP) {
    bP -> pages = (struct Page*)malloc(sizeof(Page)*maxBufferSize);
    bP->pageCount = 0;
}

// Search functions:

void findPage(BufferPool* bP, int tableNumber, int pageNumber) {
    for (int i = 0; i < bP -> pageCount; i++) {
        Page page = bP -> pages[i*sizeof(Page)];
        if (page.tableNumber == tableNumber && page.pageNumber == pageNumber) {
            return &bP -> pages[i*sizeof(Page)];
        }
    }
    return NULL;
}  // Do we want to implement this to search for data instead?