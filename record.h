#include "main.h"

typedef struct Record {
    int id;
    char name[MAX_NAME_SIZE];
    char* data;
} Record;

// void initializeRecord(Record* record);