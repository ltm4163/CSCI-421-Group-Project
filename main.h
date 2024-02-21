#include <stdio.h>
#include <stdlib.h>
#include "catalog.h"
#include "buffer.h"
#include "parse.h"

#ifndef MAIN_H
#define MAIN_H

void initializeDatabase(const char* dbLocation, int pageSize, int bufferSize);
Catalog *getCatalog(); //returns catalog variable to other files
Buffer *getBuffer(); //returns buffer pool variable to other files
char* getDbDirectory();
#endif
