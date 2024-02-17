#ifndef STORAGEMANAGER_H
#define STORAGEMANAGER_H

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "constants.h"
#include "catalog.h"
#include "page.h"
#include "record.h"

Page* getPage(int tableNumber, int pageNumber); //read desired page from hardware

Record getRecord(int tableNumber, void* primaryKey); //get record from table

void initCatalog(); //retreives catalog initiated in main.c

void initBuffer(); //retreives buffer pool initiated in main.c

#endif