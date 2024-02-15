#include "bufferpool.h"

void initializeBufferPool(BufferPool* bufferPool) {
    bufferPool -> pages = (struct Page*)malloc(sizeof(Page)*3);
    bufferPool->pageCount = 0;
}

// Search functions:

void findPage(BufferPool* bufferPool, int pageNumber) {

}  // Do we want to implement this to search for data instead?