#include "main.h"
#include "record.h"

typedef struct Page {
    struct Record records[MAX_NUM_RECORDS];
    char data[MAX_PAGE_SIZE];
    int pageNumber;
    int tableNumber;
} Page;

void initializePage(Page* page, int pageNumber, int tableNumber);