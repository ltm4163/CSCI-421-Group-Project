#include <string.h>
#include "attribute.h"

void initializeAttribute(AttributeSchema* attr, char *name, char *type, bool unique, bool nonNull, bool pK, int size) {
    strcpy(attr->name, name);
    strcpy(attr->type, type);
    attr->unique = unique;
    attr->nonNull = nonNull;
    attr->primaryKey = pK;
    attr->size = size;
}