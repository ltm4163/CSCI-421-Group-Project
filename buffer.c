#include "buffer.h"

// structure to hold the buffer
typedef struct buffer {
	uint8_t * buffer;
	size_t head;
	size_t tail;
	size_t max;
	bool full;

} Buffer;

// init the buffer
Buffer buf_init(uint8_t* loc, size_t size) {
	// allocates space for the buffer 
	Buffer buf = malloc(sizeof(Buffer));
	buf->buffer = loc;
	buf->max = size;

	// ensures the buffer is empty
	buf_reset(buf);

	return buf;

}

// resets the buffer
void buf_reset(Buffer buf) {
	buf->head = 0;
	buf->tail = 0;
	buf->full = false;
}

// checks if the buffer is full
bool buf_full(Buffer buf) { return buf->full; }

// checks if the buffer is empty 
bool buf_empty(Buffer buf) { return (!buf->full && (buf->head == buf->tail)); }

// returns the max capacity of the buffer
size_t buf_capacity(Buffer buf) { return buf->max; }

// calculates the size of the buffer
size_t buf_size(Buffer buf) {
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




