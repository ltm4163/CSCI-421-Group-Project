#ifndef BUFFERPOOL_H
#define BUFFERPOOL_H
#include "page.h"

typedef struct BufferPool{
    struct Page* pages;
    int pageCount;
} BufferPool;

void initializeBufferPool(BufferPool* bP);

// Search functions:

void findPage(BufferPool* bP, int pageNumber);  // Do we want to implement this to search for data instead?

#endif