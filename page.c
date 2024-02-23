#include "page.h"

void initializePage(Page* page, int pageNumber, int tableNumber, bool updated) {
    page->pageNumber = pageNumber;
    page->tableNumber = tableNumber;
    page->updated = updated;
    page->size = 0;
}
