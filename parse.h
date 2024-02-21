#ifndef PARSE_H_
#define PARSE_H_
#include "attribute.h"
#include "table.h"

// parses attributes
void ParseAttribute(char* attributes);

// parses table
TableSchema* ParseTable();


// main parser
void parse();


#endif
