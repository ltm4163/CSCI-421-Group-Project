#ifndef CONSTANTS_H
#define CONSTANTS_H

#define MAX_NUM_RECORDS 100  // Can change
#define MAX_NUM_ATTRIBUTES 50  // Can also change
#define MAX_NAME_SIZE 50  // "   "
// #define MAX_PAGE_SIZE(value) const int maxPageSize = value
// #define MAX_BUFFER_SIZE(value) const int MaxBufferSize = value
extern int MAX_PAGE_SIZE;


void updateValues(int pageSize, int bufferSize);


#endif
