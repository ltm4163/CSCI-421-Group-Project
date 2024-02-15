#include "constraint.h"
#include "main.h"

typedef struct Attribute {
    char name[MAX_NAME_SIZE];
    char type[20];
    struct Constraint *constraints;
} Attribute;

void initializeAttribute(Attribute* attribute);