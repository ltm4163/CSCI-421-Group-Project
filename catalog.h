#ifndef catalog.h
#define catalog.h
#include "table.h"

typedef struct Catalog {
    struct Table* tables;
    int tableCount;
} Catalog;


// Initialization Functions:

void initializeCatalog(Catalog* c);

// Insertion Functions:

void addTable(Catalog* c, char name[MAX_NAME_SIZE]);  // recordCount and currentPage initialized as 0

void addRecord(Catalog* c, int pageNumber, int id, char name[MAX_NAME_SIZE]);  // Can we auto-initialize id to the next id instead of hard-coding it in?
                                                                               // Access the Page from the provided Catalog

void addPage(Catalog* c);  // How do we deal with this one?

void addAttribute(Catalog* c, char name[MAX_NAME_SIZE], char type[20]);  // Add more constraints as needed
#endif
