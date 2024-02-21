#include <string.h>
#include <stdio.h>
#include "attribute.h"

void initializeAttribute(AttributeSchema* attr, char *name, char *type, bool unique, bool nonNull, bool pK, int size) {
    strcpy(attr->name, name);
    strcpy(attr->type, type);
    attr->unique = unique;
    attr->nonNull = nonNull;
    attr->primaryKey = pK;
    attr->size = size;
}

void displayAttribute(AttributeSchema *attr) {
    printf("%s ", attr->name);

    if(strcmp(attr->type, "char") == 0 || strcmp(attr->type, "varchar")) {
        printf("%s(%d) ", attr->type, attr->size);
    } else { printf("%s ", attr->type); }

    if(attr->unique) { printf("unique "); }
    if(attr->nonNull) { printf("notNull "); }
    if(attr->primaryKey) { printf("primaryKey"); }

}