// main driver file
// add docs here

#include <stdio.h>
#include <stdlib.h>

void initializeDatabase(const char* dbLocation, int pageSize, int bufferSize);

int main(int argc, char* argv[]) {
    if (argc != 4) {
        fprintf(stderr, "Usage: %s <db loc> <page size> <buffer size>\n", argv[0]);
        return 1;
    }

    const char* dbLocation = argv[1];
    int pageSize = atoi(argv[2]);
    int bufferSize = atoi(argv[3]);


    initializeDatabase(dbLocation, pageSize, bufferSize);

    // Main logic

    return 0;
}

void initializeDatabase(const char* dbLocation, int pageSize, int bufferSize) {
    printf("Initializing database at %s with page size %d and buffer size %d\n", dbLocation, pageSize, bufferSize);
    // do stuff here...

}

