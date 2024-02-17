#include <stdlib.h>
#include "bufferpool.h"

void initializeBufferPool(BufferPool* bP) {
    bP -> pages = (struct Page*)malloc(sizeof(Page)*3);
    bP->pageCount = 0;
}

// Search functions:

void findPage(BufferPool* bP, int pageNumber) {

}  // Do we want to implement this to search for data instead?