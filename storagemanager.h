#ifndef STORAGEMANAGER_H
#define STORAGEMANAGER_H

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "constants.h"
#include "catalog.h"
#include "page.h"
#include "record.h"

Page *getRecords(int tableNumber);

void addRecord(Catalog* c, Record record, int tableNumber); //addRecord inserts a record to a certain table of a catalog

void createRecords(Page *page, int tableNumber); // Convert page data into records using table schema

Page* getPage(int tableNumber, int pageNumber); //read desired page from hardware

Page findPage(int tableNumber, int pageNumber); //look for page in buffer pool

Record getRecord(int tableNumber, void* primaryKey); //get record from table

void initCatalog(); //retreives catalog initiated in main.c

void initBuffer(); //retreives buffer pool initiated in main.c

void initializeStorageManager(); //retrieve buffer and catalog

#endif