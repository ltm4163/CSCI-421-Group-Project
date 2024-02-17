#ifndef ATTRIBUTE_H
#define ATTRIBUTE_H
#include <stdbool.h>
#include "constraint.h"
#include "constants.h"

typedef struct Attribute {
    char name[MAX_NAME_SIZE];
    char type[20];
    bool unique;
    bool nonNull;
    bool primarKey;
} Attribute;

void initializeAttribute(Attribute* attr);

#endif