#ifndef BUFFERPOOL_H
#define BUFFERPOOL_H
#include "page.h"

typedef struct BufferPool{
    struct Page* pages;
    int pageCount;
} BufferPool;

void initializeBufferPool(BufferPool* bP);

#endif
