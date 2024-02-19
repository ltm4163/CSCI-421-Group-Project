#ifndef ATTRIBUTE_H
#define ATTRIBUTE_H
#include <stdbool.h>
#include "constraint.h"
#include "constants.h"

typedef struct AttributeSchema {
    char name[MAX_NAME_SIZE];
    char type[20];
    bool unique;
    bool nonNull;
    bool primarKey;
    int size;
} AttributeSchema;

void initializeAttribute(AttributeSchema* attr);

#endif