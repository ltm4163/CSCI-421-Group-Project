#ifndef CATALOG_H
#define CATALOG_H
#include "table.h"

typedef struct Catalog {
    TableSchema* tables;
    int tableCount;
} Catalog;


// Initialization Functions:

void initializeCatalog(Catalog* c, int count);

// Insertion Functions:

void addTable(Catalog* c, TableSchema* table);  // recordCount and currentPage initialized as 0

void dropTable(Catalog* c, char name[MAX_NAME_SIZE]);

void addPage(Catalog* c);  // How do we deal with this one?

void addAttribute(Catalog* c, char name[MAX_NAME_SIZE], char type[20]);  // Add more constraints as needed

void displayCatalog(Catalog* c);

#endif
