#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAX_PAGE_SIZE 4096  // bytes
#define MAX_NUM_RECORDS 100  // Can change
#define MAX_NUM_ATTRIBUTES 50  // Can also change
#define MAX_NAME_SIZE 50  // "   "

struct Catalog {
    struct Table* tables;
    int tableCount;
    struct BufferPool* bufferPool;
};

struct Table {
    struct Attribute attributes[MAX_NUM_ATTRIBUTES];
    char name[MAX_NAME_SIZE];
    int recordCount;
    int currentPage;
};

struct Record {
    int id;
    char name[MAX_NAME_SIZE];

};

struct BufferPool{
    struct Page* pages;
    int pageCount;
};

struct Page {
    char data[MAX_PAGE_SIZE];
};

struct Attribute {
    char name[MAX_NAME_SIZE];
    char type[20];
    // Add constraints or other things. Maybe size
};
