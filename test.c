#include "page.h"
#include "buffer.h"
#include "catalog.h"
#include "storagemanager.h"
#include <stdio.h>

void testBuffer(Buffer *buffer) { //test if buffer works properly
    Page *page1 = (Page*)malloc(sizeof(Page));
    initializePage(page1, 0, 0, false);
    Page *page2 = (Page*)malloc(sizeof(Page));
    initializePage(page2, 1, 0, false);

    buf_put(buffer, *page1);
    buf_put(buffer, *page2);

    printf("bufSize: %d\n", buf_size(buffer));
    Page *pg = (Page*)malloc(sizeof(Page));
    buf_get(buffer, pg);
    printf("pageNum: %d\n", pg->pageNumber);
    buf_get(buffer, pg);
    printf("pageNum: %d\n", pg->pageNumber);
}

void testBool() { //test if reading/writing bool type to file works
    FILE *fp = fopen("tables/1.bin", "wb");
    bool flag = false;
    fwrite(&flag, sizeof(bool), 1, fp);
    fclose(fp);
    FILE *file = fopen("tables/1.bin", "rb");
    bool test;
    fread(&test, sizeof(bool), 1, file);
    printf("%s\n", test?"true":"false");
    fclose(file);
}

void testStoreBool() { //test if page/record store bool properly
    Page *page = (Page*)malloc(sizeof(Page));
    initializePage(page, 0, 0, true);
    page->data = (void*)malloc(sizeof(bool));
    bool value = false;
    memcpy(page->data, &value, sizeof(bool));
    bool pageTest;
    memcpy(&pageTest, page->data, sizeof(bool));
    printf("%s\n", pageTest?"true":"false");
    free(page);

    Record *rec = (Record*)malloc(sizeof(Record));
    rec->data = (void*)malloc(sizeof(bool));
    value = true;
    memcpy(rec->data, &value, sizeof(bool));
    bool recTest;
    memcpy(&recTest, rec->data, sizeof(bool));
    printf("%s\n", recTest?"true":"false");
    free(rec);
}

void testGetRecords(Buffer *buffer, Catalog *cat) {
    FILE *fp;
    fp = fopen("tables/0.bin","wb");
    void *toWrite1 = malloc(MAX_PAGE_SIZE);
    char *text1 = malloc(11);
    strcpy(text1, "more text!");
    int int1 = 5;
    int numRecords = 1;
    int numPages = 1;
    bool flag = false;
    memcpy(toWrite1, &numPages, sizeof(int));
    memcpy(toWrite1+sizeof(int), &numRecords, sizeof(int));
    memcpy(toWrite1+(2*sizeof(int)), &int1, sizeof(int));
    memcpy(toWrite1+(3*sizeof(int)), text1, 11);
    memcpy(toWrite1+(3*sizeof(int))+11, &flag, sizeof(bool));
    fwrite(toWrite1, MAX_PAGE_SIZE, 1, fp);
    fclose(fp);
    
    AttributeSchema *attributes = (AttributeSchema*)malloc(sizeof(AttributeSchema)*3);
    AttributeSchema *attr1 = (AttributeSchema*)malloc(sizeof(AttributeSchema));
    initializeAttribute(attr1, "num", "integer", false, false, true, sizeof(int));
    AttributeSchema *attr2 = (AttributeSchema*)malloc(sizeof(AttributeSchema));
    initializeAttribute(attr2, "words", "char", false, false, false, 11);
    AttributeSchema *attr3 = (AttributeSchema*)malloc(sizeof(AttributeSchema));
    initializeAttribute(attr3, "flag", "boolean", false, false, false, sizeof(bool));
    attributes[0] = *attr1;
    attributes[1] = *attr2;
    attributes[2] = *attr3;
    TableSchema *table = (TableSchema*)malloc(sizeof(TableSchema));
    initializeTable(table, 3, "table1", attributes);
    table->tableNumber = 0;
    table->numPages = 1;
    cat->tables[0] = *table;
    // Page *pg = getPage(0, 0);
    // buf_put(buffer, *pg);
    // Record *rec = pg->records[0];
    getRecords(0);
}