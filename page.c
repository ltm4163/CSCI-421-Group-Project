#include "page.h"
#include <stdlib.h>

void initializePage(Page* page, int pageNumber, int tableNumber, bool updated) {
    page->pageNumber = pageNumber;
    page->tableNumber = tableNumber;
    page->updated = updated;
    page->size = sizeof(int); //accounts for numRecs int
    page->records = (Record**)malloc(100*sizeof(Record*));
}
