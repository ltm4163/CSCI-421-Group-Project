#ifndef PARSE_H_
#define PARSE_H_

// buffer struct
typedef struct Buffer Buffer;

// resets the buffer
void buf_reset(Buffer buf);

// add data to the buffer
void buf_put(Buffer buf, Page page);

// adds data, doesn't add if buffer is full
int buf_putr(Buffer buf, Page page);

// gets a page from a buffer
Page buf_get(Buffer buf, Page *page);

// checks if the buffer is empty
bool buf_empty(Buffer buf);

// checks if the buffer is full
bool buf_full(Buffer buf);

// returns max capacity of a buffer 
size_t buf_capacity(Buffer buf);

// returns the size of the buffer
size_t buf_size(Buffer buf);

#endif 
