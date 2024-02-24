#ifndef ATTRIBUTE_H
#define ATTRIBUTE_H
#include <stdbool.h>
#include "constraint.h"
#include "constants.h"
#include <stdio.h>

typedef struct AttributeSchema {
    char name[MAX_NAME_SIZE];
    char type[20];
    bool unique;
    bool nonNull;
    bool primaryKey;
    int size; //size of attr type
} AttributeSchema;

void initializeAttribute(AttributeSchema* attr, char *name, char *type, bool unique, bool nonNull, bool pK, int size);

void displayAttribute(AttributeSchema* attr);

#endif