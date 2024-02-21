#ifndef PARSE_H_
#define PARSE_H_

// parses attributes
AttributeSchema* ParseAttribute(char* attributes);

// parses table
TableSchema* ParseTable();


// main parser
void parse();


#endif
