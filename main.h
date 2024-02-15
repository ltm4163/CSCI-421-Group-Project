#include <stdio.h>
#include <stdlib.h>
#include "storagemanager.h"

#define MAX_PAGE_SIZE 4096  // bytes
#define MAX_NUM_RECORDS 100  // Can change
#define MAX_NUM_ATTRIBUTES 50  // Can also change
#define MAX_NAME_SIZE 50  // "   "

Catalog *getCatalog();
BufferPool *getBufferPool();