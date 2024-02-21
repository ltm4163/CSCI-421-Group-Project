#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "catalog.h"


// Initialization Functions:

void initializeCatalog(Catalog* c) {
    c -> tables = (TableSchema*)malloc(sizeof(TableSchema));
}


// Insertion Functions:

void addTable(Catalog* c, char name[MAX_NAME_SIZE]) {

}  // recordCount and currentPage initialized as 0

void addPage(Catalog* c) {

}  // How do we deal with this one?

void addAttribute(Catalog* c, char name[MAX_NAME_SIZE], char type[20]) {

}  // Add more constraints as needed

void writeCatalogToFile(Catalog* c, char* pathname) {
    FILE* file = fopen(pathname, "wb");

    fwrite(c, sizeof(Catalog), 1, file);

    fwrite(c -> tables, sizeof(TableSchema), 1, file);

    fwrite(c -> tables -> attributes, sizeof(AttributeSchema), 1, file);

    fclose(file);
}

void readCatalogFromFile(Catalog* c, char* pathname) {
    FILE* file = fopen(pathname, "rb");

    fread(c, sizeof(Catalog), 1, file);

    c -> tables = (TableSchema*)malloc(sizeof(TableSchema));
    fread(c -> tables, sizeof(TableSchema), 1, file);

    c -> tables -> attributes = (AttributeSchema*)malloc(sizeof(AttributeSchema));
    fread(c -> tables -> attributes, sizeof(AttributeSchema), 1, file);

    fclose(file);
}