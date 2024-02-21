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
// if the buffer is full, doesn't put
int buf_putr(Buffer* buf, Page data) {
	if(!buf_full(buf)) {
		buf->buffer[buf->head] = data;
		adv_pointer(buf);
		return 0;
	}

	return -1;

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


void writeBufferToHardware(Buffer* bPool) {
    printf("Starting to write to hardware...\n");

    for (size_t i = 0; i < buf_size(bPool); ++i) {
        printf("\n---\n");
        Page* page = &bPool->buffer[i];
        if (!page->updated) continue; // Only write pages marked as updated

        char filename[256];
        snprintf(filename, sizeof(filename), "%s/tables/%d.bin", getDbDirectory(), page->tableNumber);

        FILE* file = fopen(filename, "rb+");
        if (!file) file = fopen(filename, "wb+"); // Create if doesn't exist
        if (!file) {
            perror("Failed to open file");
            continue;
        }

        long offset = (long)page->pageNumber * MAX_PAGE_SIZE;
        fseek(file, offset, SEEK_SET);

        if (fwrite(page->data, MAX_PAGE_SIZE, 1, file) != 1) {
            perror("Failed to write data");
        } else {
            printf("Page %zu for table %d written to disk.\n", i, page->tableNumber);
        }

        fclose(file);
        page->updated = false; // Reset the flag after writing
    }

    printf("Finished writing to hardware.\n");
}
