#include "buffer.h"
#include "page.h"
#include "main.h"
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>

// init the buffer
Buffer *buf_init(Page* data, size_t size) {
	// allocates space for the buffer 
	Buffer* buf = malloc(sizeof(Buffer));
	buf->buffer = data;
	buf->max = size;

	// ensures the buffer is empty
	buf_reset(buf);

	return buf;

}

// resets the buffer
void buf_reset(Buffer* buf) {
	buf->head = 0;
	buf->tail = 0;
	buf->full = false;
}

// checks if the buffer is full
bool buf_full(Buffer* buf) { return buf->full; }

// checks if the buffer is empty 
bool buf_empty(Buffer* buf) { return (!buf->full && (buf->head == buf->tail)); }

// returns the max capacity of the buffer
size_t buf_capacity(Buffer* buf) { return buf->max; }

// calculates the size of the buffer
size_t buf_size(Buffer* buf) {
	// if the buffer is full, size is just max
	size_t size = buf->max;
	
	// if not full, calculate using head/tail
	if(!buf->full) {
		if(buf->head >= buf->tail) {
			size = (buf->head - buf->tail);
		} else {
			size = (buf->max + buf->head - buf->tail);
		}

	}

	return size;
}

// helper function to advance pointer
static void adv_pointer(Buffer* buf) {
	if(buf->full) {
		buf->tail = (buf->tail + 1) % buf->max;
	} 

	buf->head = (buf->head + 1) % buf->max;
	buf->full = (buf->head == buf->tail);
}

// helper function to retreat pointer
static void re_pointer(Buffer* buf) {
	buf->full = false;
	if(++(buf->tail) == buf->max) {
		buf->tail = 0;

	}

}

// put a new element in the buffer
void buf_put(Buffer* buf, Page data) {
	buf->buffer[buf->head] = data;
	adv_pointer(buf);
}

// put a new element in the buffer
// if the buffer is full, write the LRU page to hardware
// Attempt to put a new page in the buffer, writing the LRU page to hardware if necessary.
int buf_putr(Buffer* buf, Page data) {
    if (buf_full(buf)) {
        // Buffer is full; find the LRU page and write to hardware if updated
        Page* lruPage = &buf->buffer[buf->tail];
        if (lruPage->updated) {
            writePageToHardware(lruPage);
            lruPage->updated = false;
            // free(lruPage);
        }

        // Advance the tail to "remove" the LRU page from the buffer.
        // TODO: Test this!
        re_pointer(buf);
    }

    // Insert the new page at the current head position.
    buf->buffer[buf->head] = data;
    data.updated = true;
    adv_pointer(buf);

    return 0;
}


// gets an element from the buffer
int buf_get(Buffer* buf, Page* data) {
	if(!buf_empty(buf)) {
		*data = buf->buffer[buf->tail];
		re_pointer(buf);
		return 0;
	}

	return -1;

}

// Called when the buffer is full
// Should write the least recently used page to hardware
void writePageToHardware(Page* page) {
    if (!page || !page->updated) return;

    char filename[256];
    snprintf(filename, sizeof(filename), "%s/tables/%d.bin", getDbDirectory(), page->tableNumber);

    FILE* file = fopen(filename, "rb+"); // Append mode, binary
    if (!file) {
        perror("Failed to open file for writing");
        return;
    }

    // write numPages to front of table file
    Catalog *cat = getCatalog();
    TableSchema *table = &cat->tables[page->tableNumber];
    fwrite(&table->numPages, sizeof(int), 1, file);

    long offset = ((long)table->pageLocations[page->pageNumber] * maxPageSize)+sizeof(int); //used to know where to write in table file

    // write numRecords to front of page.data
    memcpy(page->data, &page->numRecords, sizeof(int));
    
    // iterate through records and write their data to page.data
    int pageOffset = sizeof(int); //used to know where to write in page.data
    for (int i = 0; i < page->numRecords; i++)
    {
        memcpy(page->data+pageOffset, page->records[i]->data, page->records[i]->size);
        pageOffset += page->records[i]->size;
    }
    
    // write page.data to table file
    fseek(file, offset, SEEK_SET);
    if (fwrite(page->data, maxPageSize, 1, file) != 1) {
        perror("Failed to write page to hardware");
    } else {
        printf("Page for table %d at pageNumber %d written to disk.\n", page->tableNumber, page->pageNumber);
    }

    fclose(file);
    page->updated = false; // Reset the flag
}


// Called when the system is shutting down
// Writes everything in the buffer to hardware
void writeBufferToHardware(Buffer* bPool) {
    printf("Starting to write to hardware...\nBuffer size: %zu\n", buf_size(bPool));

    for (size_t i = 0; i < buf_size(bPool); ++i) {
        Page* page = &bPool->buffer[i];
        printf("Page %zu: updated=%d\n", i, page->updated);
        
        if (!page->updated) continue;

        char filename[256];
        snprintf(filename, sizeof(filename), "%s/tables/%d.bin", getDbDirectory(), page->tableNumber);
        printf("Filename: %s\n", filename);

        FILE* file = fopen(filename, "ab+");
        if (!file) {
            perror("Failed to open file");
            continue;
        }

        Catalog *cat = getCatalog();
        long offset = (long)cat->tables[page->tableNumber].pageLocations[page->pageNumber] * maxPageSize;
        if (fseek(file, offset, SEEK_SET) != 0) {
            perror("Seek failed");
            fclose(file);
            continue;
        }

        printf("Writing at offset: %ld, MAX_PAGE_SIZE: %d\n", offset, maxPageSize);
        size_t written = fwrite(page->data, maxPageSize, 1, file);
        if (written != 1) {
            perror("Failed to write data");
        } else {
            printf("Page %zu for table %d written to disk. Bytes written: %zu\n", i, page->tableNumber, written * maxPageSize);
        }

        fclose(file);
        page->updated = false;
    }

    printf("Finished writing to hardware.\n");
}

// Used for the drop function
void clearTablePagesFromBuffer(Buffer* bPool, int tableNumber) {
    for (size_t i = 0; i < buf_size(bPool); ++i) {
        Page* page = &bPool->buffer[i];
        if (page->tableNumber == tableNumber) {
            if (page->updated) {
                writePageToHardware(page);
            }
            
            // TODO: Check that this clears the page data successfully
            memset(page, 0, sizeof(Page));
        }
    }
}
