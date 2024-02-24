// main driver file
// add docs here
#include "storagemanager.h"
#include "catalog.h"
#include "constants.h"
#include "attribute.h"
#include "page.h"
#include "buffer.h"
#include "parse.h"
#include "test.c"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/stat.h>
#include "main.h"

Catalog *cat;
Buffer *buffer;
char* dbDirectory = NULL;
int pageSize;
int bufferSize;

int fileExists(const char* path) {
    struct stat buffer;
    return (stat(path, &buffer) == 0);
}

int main(int argc, char* argv[]) {
    if (argc != 4) {
        printf("Usage: %s <db location> <page size> <buffer size>\n", argv[0]);
        return 1;
    }
    
    char tablesDir[128]; // holds path to tables directory

    const char* dbLocation = argv[1];
    pageSize = atoi(argv[2]);
    bufferSize = atoi(argv[3]);

    // Compute path for catalog
    char catalogPath[256];
    snprintf(catalogPath, sizeof(catalogPath), "%s/catalog.bin", dbLocation);

    printf("Welcome to JottQL\n");
    printf("Looking for catalog at %s...\n", catalogPath);

    dbDirectory = strdup(dbLocation);
    if (dbDirectory == NULL) {
        perror("Failed to allocate memory for dbDirectory");
        return 1;
    }

    cat = malloc(sizeof(Catalog));
    initializeCatalog(cat);
    
    if (fileExists(catalogPath)) {
        printf("Existing db found at %s\n", dbLocation);
        readCatalogFromFile(cat, catalogPath);
    } else {
        printf("No existing db found at %s\n", dbLocation);
    }
    
    Page* buf = malloc(bufferSize * sizeof(Page));
    buffer = buf_init(buf, bufferSize);
    //buffer = buf_init(buf, 1); // for testing

    updateValues(pageSize, bufferSize);

    initializeStorageManager();
   
    printf("Page size: %d\n", pageSize);
    printf("Buffer size: %d\n", bufferSize);
    printf("\nPlease enter commands, enter <quit> to shutdown the db\n\n");

    //Testing begin
    // testGetRecords(buffer, cat, dbDirectory, pageSize);
    // testInsert(buffer, cat, dbDirectory);
    testDoubleInsert(buffer, cat, dbDirectory, pageSize);
    //Testing end

    // 0 = false, 1 = true
    int shouldExit = 0;
    
    char inputLine[1024]; // Buffer to store user input

    while (1) {
        printf("JottQL> ");
        if (fgets(inputLine, sizeof(inputLine), stdin) == NULL) break; // Check for EOF or error
        if (parse(inputLine, catalogPath)) break; // Exit loop if parse returns 1 (quit command)
    }

    return 0;
}


Catalog *getCatalog() { //returns catalog variable to other files
    return cat;
}

Buffer* getBuffer() { //returns buffer pool variable to other files
    return buffer;
}

char* getDbDirectory() {
    return dbDirectory;
}

int getPageSize() {
    return pageSize;
}

int getBufferSize() {
    return bufferSize;
}
