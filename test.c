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

void testGetRecords(Buffer *buffer, Catalog *cat, char *dbDirectory) {
    FILE *fp;
    char filename[256];
    snprintf(filename, sizeof(filename), "%s/tables/%d.bin", dbDirectory, 0);
    fp = fopen(filename,"wb");
    void *toWrite1 = malloc(MAX_PAGE_SIZE);
    char *text1 = malloc(14);
    strcpy(text1, "more text!");
    int int1 = 5;
    int numRecords = 2;
    int numPages = 1;
    bool flag = false;
    char *text2 = malloc(14);
    strcpy(text2, "sample text:)");
    int int2 = 23;
    bool flag2 = true;
    memcpy(toWrite1, &numPages, sizeof(int));
    memcpy(toWrite1+sizeof(int), &numRecords, sizeof(int));
    memcpy(toWrite1+(2*sizeof(int)), &int1, sizeof(int));
    memcpy(toWrite1+(3*sizeof(int)), text1, 14);
    memcpy(toWrite1+(3*sizeof(int))+14, &flag, sizeof(bool));
    memcpy(toWrite1+(3*sizeof(int))+14+sizeof(bool), &int2, sizeof(int));
    memcpy(toWrite1+(4*sizeof(int))+14+sizeof(bool), text2, 14);
    memcpy(toWrite1+(4*sizeof(int))+28+sizeof(bool), &flag2, sizeof(bool));
    fwrite(toWrite1, MAX_PAGE_SIZE, 1, fp);
    fclose(fp);
    
    AttributeSchema *attributes = (AttributeSchema*)malloc(sizeof(AttributeSchema)*3);
    AttributeSchema *attr1 = (AttributeSchema*)malloc(sizeof(AttributeSchema));
    initializeAttribute(attr1, "num", "integer", false, false, true, sizeof(int));
    AttributeSchema *attr2 = (AttributeSchema*)malloc(sizeof(AttributeSchema));
    initializeAttribute(attr2, "words", "char", false, false, false, 14);
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

void testBufferWrite(Buffer *buffer, Catalog *cat) {
    AttributeSchema *attributes = (AttributeSchema*)malloc(sizeof(AttributeSchema)*3);
    AttributeSchema *attr1 = (AttributeSchema*)malloc(sizeof(AttributeSchema));
    initializeAttribute(attr1, "num", "integer", false, false, true, sizeof(int));
    AttributeSchema *attr2 = (AttributeSchema*)malloc(sizeof(AttributeSchema));
    initializeAttribute(attr2, "words", "char", false, false, false, 14);
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

    Page *page = getPage(0, 0);
    printf("numRecs1: %d\n", page->numRecords);
    int int3 = 2;
    char *text3 = malloc(14);
    strcpy(text3, "third");
    bool flag3 = false;
    Record *rec = (Record*)malloc(sizeof(Record));
    rec->data = (void*)malloc(sizeof(int) + 14 + sizeof(bool));
    memcpy(rec->data, &int3, sizeof(int));
    memcpy(rec->data+sizeof(int), text3, 14);
    memcpy(rec->data+sizeof(int)+14, &flag3, sizeof(bool));
    rec->size = sizeof(int) + 14 + sizeof(bool);
    page->records[2] = rec;
    page->numRecords++;
    page->updated = true;
    buf_putr(buffer, *page);
    Page *empty = (Page*)malloc(sizeof(Page));
    initializePage(empty, 1, 0, false);
    buf_putr(buffer, *empty);

    Page *test = getPage(0, 0);
    printf("numRecs2: %d\n", test->numRecords);
    int test2;
    memcpy(&test2, page->records[2]->data, sizeof(int));
    printf("int: %d\n", test2);
}