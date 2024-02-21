#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "catalog.h"


// Initialization Functions:

void initializeCatalog(Catalog* c, int count) {
    c -> tables = (TableSchema*)malloc(sizeof(TableSchema));
    c->tableCount = count;
}


// Insertion Functions:

void addTable(Catalog* c, TableSchema* table) {
    TableSchema* newTables = realloc(c->tables, (c->tableCount + 1) * sizeof(TableSchema));
    if(newTables != NULL) {
        newTables[c->tableCount] = *table;
        c->tables = newTables;
        c->tableCount++;
        printf("added table: %s\n", table->name);
    } else { fprintf(stderr, "Memory allocation failed\n"); }

}  // recordCount and currentPage initialized as 0

void dropTable(Catalog* c, char name[MAX_NAME_SIZE]) {
    int tableIndex = -1;

    for(int i = 0; i < c->tableCount; i++) {
        if(strcmp(c->tables[i].name, name) == 0) {
            tableIndex = i;
            break;
        }
    }

    if(tableIndex != -1) {
        for(int i = tableIndex; i < c->tableCount - 1; i++) {
            c->tables[i] = c->tables[i+1];
        }

        c->tableCount--;

        TableSchema* newTables = realloc(c->tables, c->tableCount * sizeof(TableSchema));

        if(newTables != NULL || c->tableCount == 0) {
            c->tables = newTables;
        } else {
            fprintf(stderr, "Table '%s' not found in catalog\n", name);
        }
    }
}

void addPage(Catalog* c) {

}  // How do we deal with this one?

void addAttribute(Catalog* c, char name[MAX_NAME_SIZE], char type[20]) {

}  // Add more constraints as needed

void displayCatalog(Catalog* c) {
    fprintf("Displaying %s", c->tableCount);
    for(int i = 0; i < c->tableCount; i++) {
        displayTable(&(c->tables[i]));
    }
    
    if(c->tableCount == 0) {
        printf("No such table\n");  // TODO: Do I need to include table name?
        printf("ERROR\n\n");
    }
}

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
