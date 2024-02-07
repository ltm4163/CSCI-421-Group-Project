#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "catalog.h"

struct Record * getrecords(Catalog catalog)
{
    int tablenumber;
    printf("Enter a table number: ");
    scanf("%d", &tablenumber);
    if(tablenumber < 0 && tablenumber >= sizeof(catalog->tables)){
        printf("Invalid input try again: ");
        scanf("%d")
    }
}

void insertrecord()
{

}