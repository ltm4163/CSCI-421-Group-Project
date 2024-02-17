#include <stdio.h>
#include <stdlib.h>
#include "catalog.h"
#include "bufferpool.h"

void initializeDatabase(const char* dbLocation, int pageSize, int bufferSize);
Catalog *getCatalog(); //returns catalog variable to other files
BufferPool *getBufferPool(); //returns buffer pool variable to other files