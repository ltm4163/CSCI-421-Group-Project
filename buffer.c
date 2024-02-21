#include "buffer.h"
#include "page.h"
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>

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
Page *buf_get(Buffer* buf, Page* data) {
	if(!buf_empty(buf)) {
		*data = buf->buffer[buf->tail];
		re_pointer(buf);
		return 0;
	}

	return -1;

}





