#include "page.h"

void initializePage(Page* page, int pageNumber, int tableNumber) {
    page->pageNumber = pageNumber;
    page->tableNumber = tableNumber;
}