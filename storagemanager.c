#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "catalog.h"

struct Record * getrecords(Catalog catalog)
{
    int tablenumber;
    printf("Enter a table number: ");
    scanf("%d", &tablenumber);
    while(tablenumber < 0 && tablenumber >= sizeof(catalog->tables)){
        printf("Invalid input try again: ");
        scanf("%d",tablenumber);
    }
    Table table=Catalog->tables[tablenumber];
    Record * returnrecords=table->records;
    return returnrecords;
}

void insertrecord()
{

}