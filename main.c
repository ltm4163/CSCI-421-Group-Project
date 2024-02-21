// main driver file
// add docs here
#include "storagemanager.h"
#include "catalog.h"
#include "constants.h"
#include "attribute.h"
#include "page.h"
#include "buffer.h"
#include "parse.h"
#include <stdio.h>
#include <stdlib.h>
#include "main.h"


Catalog *cat;
Buffer *buffer;

int main(int argc, char* argv[]) {
    cat = malloc(sizeof(Catalog));
    initializeCatalog(cat, 0);

    Page* buf = malloc(maxBufferSize * sizeof(Page));
    buffer = buf_init(buf, maxBufferSize);
    printf("Buffer size: %zu\n", buf_size(buffer));


    while(1) {
        parse();
    }




    return 0;
}

//void initializeDatabase(const char* dbLocation, int pageSize, int bufferSize) {
//    printf("Initializing database at %s with page size %d and buffer size %d\n", dbLocation, pageSize, bufferSize);
    // do stuff here...

//}

Catalog *getCatalog() { //returns catalog variable to other files
    return cat;
}

Buffer* getBuffer() { //returns buffer pool variable to other files
    return buffer;
}

