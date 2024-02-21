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
    
    Page* buf = malloc(bufferSize * sizeof(Page)); // Adjust bufferSize usage
    buffer = buf_init(buf, bufferSize);
    printf("Buffer size: %d\n", bufferSize); // Changed to match command line input

    // Parse user input
    while(1) {
        parse();
    }

    // Cleanup
    free(cat);
    free(buf);

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

