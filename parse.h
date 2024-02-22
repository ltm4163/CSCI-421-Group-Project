#include "attribute.h"
#include "table.h"
#ifndef PARSE_H
#define PARSE_H

// main parser
int parse(char* inputLine);

// parses attributes
void ParseAttribute(char* attributes);

// parses table
TableSchema* ParseTable();



#endif
