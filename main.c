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

// Function to check if a directory exists
int directoryExists(const char* path) {
    struct stat stats;
    if (stat(path, &stats) == 0 && S_ISDIR(stats.st_mode)) {
        return 1;
    }
    return 0;
}

// Checks/creates db location and a path for tables
void ensureDbDirectory(const char* dbLocation) {
    struct stat st = {0};

    if (stat(dbLocation, &st) == -1) {
        // mkdir(dbLocation, 0755);
        mkdir(dbLocation);
    }
    
    char tablesDirPath[256];
    snprintf(tablesDirPath, sizeof(tablesDirPath), "%s/tables", dbLocation);
    
    if (stat(tablesDirPath, &st) == -1) {
        // mkdir(tablesDirPath, 0755);
        mkdir(dbLocation);
    }
}

int createDirectory(const char* path, mode_t mode) {
    struct stat st = {0};

    if (stat(path, &st) == -1) {
        // if (mkdir(path, mode) == -1) {
        if (mkdir(path) == -1) {
            perror("Failed to create directory");
            return -1;
        }
    }

    return 0;
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

    printf("Welcome to JottQL\n");
    printf("Looking at %s for existing db....\n", dbLocation);

    dbDirectory = strdup(dbLocation); // Allocate and copy dbLocation to global variable
    if (dbDirectory == NULL) {
        perror("Failed to allocate memory for dbDirectory");
        return 1;
    }
    
    if (!directoryExists(dbLocation)) {
        printf("No existing db found\n");
        printf("Creating new db at %s\n", dbLocation);
        ensureDbDirectory(dbLocation);
        printf("New db created successfully\n");
    } else {
        printf("Existing db found at %s\n", dbLocation);
        // Existing db path is what the user passed in
    }
    
    cat = malloc(sizeof(Catalog));
    initializeCatalog(cat, 0);
    
    Page* buf = malloc(bufferSize * sizeof(Page));
    buffer = buf_init(buf, bufferSize);

    initializeStorageManager();
   
    printf("Page size: %d\n", pageSize);
    printf("Buffer size: %d\n", bufferSize);
    printf("\nPlease enter commands, enter <quit> to shutdown the db\n\n");

    //Testing begin
    //testGetRecords(buffer, cat);
    //Testing end

    // 0 = false, 1 = true
    int shouldExit = 0;
    
    char inputLine[1024]; // Buffer to store user input

    while (1) {
        printf("JottQL> ");
        if (fgets(inputLine, sizeof(inputLine), stdin) == NULL) break; // Check for EOF or error
        if (parse(inputLine)) break; // Exit loop if parse returns 1 (quit command)
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
