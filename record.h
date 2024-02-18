#ifndef RECORD_H
#define RECORD_H
#include "constants.h"

typedef struct Record {
    int id;
    char name[MAX_NAME_SIZE];
    char* data;
} Record;

// void initializeRecord(Record* rec);

#endif
