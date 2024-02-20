#include <stdlib.h>
#include "bufferpool.h"

void initializeBufferPool(BufferPool* bP) {
    bP -> pages = (struct Page*)malloc(sizeof(Page)*maxBufferSize);
    bP->pageCount = 0;
}
