#include "page.h"

typedef struct BufferPool{
    struct Page* pages;
    int pageCount;
} BufferPool;

void initializeBufferPool(BufferPool* bufferPool);

// Search functions:

void findPage(BufferPool* bufferPool, int pageNumber);  // Do we want to implement this to search for data instead?