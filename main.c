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
#include <sys/stat.h>
#include "main.h"

Catalog *cat;
Buffer *buffer;

// Function to check if a directory exists
int directoryExists(const char* path) {
    struct stat stats;
    if (stat(path, &stats) == 0 && S_ISDIR(stats.st_mode)) {
        return 1;
    }
    return 0;
}

int main(int argc, char* argv[]) {
    if (argc != 4) {
        printf("Usage: %s <db location> <page size> <buffer size>\n", argv[0]);
        return 1;
    }

    const char* dbLocation = argv[1];
    int pageSize = atoi(argv[2]);
    int bufferSize = atoi(argv[3]);

    printf("Welcome to JottQL\n");
    printf("Looking at %s for existing db....\n", dbLocation);

    if (!directoryExists(dbLocation)) {
        printf("No existing db found\n");
        printf("Creating new db at %s\n", dbLocation);
        // TODO: create new database
        printf("New db created successfully\n");
    } else {
        printf("Existing db found at %s\n", dbLocation);
        // TODO: load the existing database
    }
    
    cat = malloc(sizeof(Catalog));
    initializeCatalog(cat, 0);
    
    Page* buf = malloc(bufferSize * sizeof(Page));
    buffer = buf_init(buf, bufferSize);
   
    printf("Page size: %d\n", pageSize);
    printf("Buffer size: %d\n", bufferSize);
    printf("\nPlease enter commands, enter <quit> to shutdown the db\n\n");

    // 0 = false, 1 = true
    int shouldExit = 0;
    
    while(!shouldExit) {
        printf("JottQL> ");
        shouldExit = parse();
    }

    return 0;
}

Catalog *getCatalog() { //returns catalog variable to other files
    return cat;
}

Buffer* getBuffer() { //returns buffer pool variable to other files
    return buffer;
}

