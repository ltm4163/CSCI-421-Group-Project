// main driver file
// add docs here

#include <stdio.h>
#include <stdlib.h>
#include "main.h"
#include "storagemanager.h"
#include "catalog.h"
#include "bufferpool.h"
#include "constants.h"

Catalog *cat;
BufferPool *pool;

int main(int argc, char* argv[]) {
    /*if (argc != 4) {
        fprintf(stderr, "Usage: %s <db loc> <page size> <buffer size>\n", argv[0]);
        return 1;
    }

    const char* dbLocation = argv[1];
    int pageSize = atoi(argv[2]);
    int bufferSize = atoi(argv[3]);


    initializeDatabase(dbLocation, pageSize, bufferSize);

    // Main logic

    return 0;*/

    FILE *fp;
    // void *toWrite = (void*)malloc(MAX_PAGE_SIZE);
    // char *text = (char*)malloc(55);
    // strcpy(text,"sample. asjknwq testing lots of text please please ahh");
    // int ex = sizeof(text);
    // printf("Int: %d\n", ex);
    // printf("Text size: %d\n", 55);
    // printf("Text: %s\n", text);
    // memcpy(toWrite, &ex, 4);
    // memcpy(toWrite+4, text, ex);
    fp = fopen("tables/5.bin","wb");
    // fwrite(toWrite, MAX_PAGE_SIZE, 1, fp);
    // //free(text);

    void *toWrite1 = (void*)malloc(MAX_PAGE_SIZE);
    char *text1 = (char*)malloc(11);
    strcpy(text1, "more text!");
    int ex1 = sizeof(text1);
    bool flag = true;
    printf("Int: %d\n", ex1);
    printf("Text size: %d\n", 11);
    printf("Text: %s\n", text1);
    memcpy(toWrite1, &ex1, 4);
    memcpy(toWrite1+4, text1, ex1);
    memcpy(toWrite1+4+ex1, &flag, sizeof(bool));
    fwrite(toWrite1, MAX_PAGE_SIZE, 1, fp);
    fclose(fp);

    // FILE *fp;
    // char *text = (char*)malloc(MAX_PAGE_SIZE);
    // strcpy(text,"sample. asjknwq testing lots of text please please ahh");
    // printf("Text: %s\n", text);
    // fp = fopen("tables/5.bin","wb");
    // fwrite(text, MAX_PAGE_SIZE, 1, fp);

    // char *text1 = (char*)malloc(MAX_PAGE_SIZE);
    // strcpy(text1, "more text!");
    // printf("Text: %s\n", text1);
    // fwrite(text1, MAX_PAGE_SIZE, 1, fp);
    // getPage(5, 2);
    // fclose(fp);

    //MAX_PAGE_SIZE = 4096;
    cat= (Catalog*)malloc(sizeof(Catalog));
    initializeCatalog(cat);
    BufferPool *pool = (BufferPool*)malloc(sizeof(BufferPool));
    initializeBufferPool(pool);
    initializeStorageManager();
    Page *pg = getPage(5, 0);
    pool->pages = pg;


    // Go into a loop asking for user commands
    // The 'quit' command terminates the loop
    char *buffer = NULL;
    size_t buffsize;
    ssize_t read = 0;
    const char *exits = "exit";

    // while (1) {
    //     printf("Enter a command: ");

    //     read = getline(&buffer, &buffsize, stdin);
    //     if (read == -1) {
    //         printf("Failure found from getline()\n");
    //         exit(EXIT_FAILURE);
    //     }

    //     if (read > 0 && buffer[read-1] == '\n') {
    //         buffer[read-1] = '\0';
    //     }

    //     if (!*buffer || strcmp(buffer, exits) == 0) {
    //         break;
    //     }

    //     printf("%zu characters were read.\n", read);
    //     printf("You typed: '%s' \n", buffer);

    //     free(buffer);
    //     buffer = NULL;
    // }








    return 0;
}

void initializeDatabase(const char* dbLocation, int pageSize, int bufferSize) {
    printf("Initializing database at %s with page size %d and buffer size %d\n", dbLocation, pageSize, bufferSize);
    // do stuff here...

}

Catalog *getCatalog() { //returns catalog variable to other files
    return cat;
}

BufferPool *getBufferPool() { //returns buffer pool variable to other files
    return pool;
}

