#ifndef CATALOG_H
#define CATALOG_H
#include "table.h"

typedef struct Catalog {
    struct TableSchema* tables;
} Catalog;


// Initialization Functions:

void initializeCatalog(Catalog* c);

// Insertion Functions:

void addTable(Catalog* c, char name[MAX_NAME_SIZE]);  // recordCount and currentPage initialized as 0

void addPage(Catalog* c);  // How do we deal with this one?

void addAttribute(Catalog* c, char name[MAX_NAME_SIZE], char type[20]);  // Add more constraints as needed

/// @brief Writes the contents of the provided Catalog pointer to the provided pathname
/// @param c The catalog with data to write (everything must be initialized/malloc'd)
/// @param pathname The path of the .bin file to write to (if it does not exist, it will be created)
void writeCatalogToFile(Catalog* c, char* pathname);

/// @brief Reads the contents in the provided pathname to the provided Catalog pointer
/// @param c The catalog to receive the data (everything must be initialized/malloc'd)
/// @param pathname The path of the file
void readCatalogFromFile(Catalog* c, char* pathname);

#endif
