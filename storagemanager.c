#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "catalog.h"

struct Page* getPage(char table[MAX_NAME_SIZE], int pageNumber) {
    FILE *file = fopen("test.bin", "rb+");
    printf("File name: %s.bin\n", table);

    if (file == NULL) {
        printf("Error opening the file.\n");
        return;
    }

    int address = (pageNumber-1)*MAX_PAGE_SIZE;
    char page_buffer[MAX_PAGE_SIZE];
    fseek(file, address, SEEK_SET);
    fread(page_buffer, MAX_PAGE_SIZE, 1, file);
    fclose(file);

    struct Page *p = (struct Page*)malloc(sizeof(struct Page));
    if (p == NULL) {
        printf("Memory allocation failed.\n");
        return;
    }
    strcpy(p->data, page_buffer);
    return p;
}