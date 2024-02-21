#include "constants.h"
#include <stddef.h>

// Default constant values
size_t maxPageSize = 42;
size_t maxBufferSize = 10;

// Update constants
// User input dictates pageSize/bufferSize
void updateValues(int pageSize, int bufferSize) {
    maxPageSize = pageSize;
    maxBufferSize = bufferSize;
}
