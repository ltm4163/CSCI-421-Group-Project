#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "parse.h"
#include "main.h"
#include "attribute.h"
#include "table.h"

AttributeSchema* ParseAttribute(char* attributes) {
	int attribute_count = 0;
	char *ptr1, *ptr2;
	char* name[MAX_NAME_SIZE];
	char* type[MAX_NAME_SIZE];
	char* constraints[MAX_NAME_SIZE];
	AttributeSchema* attribute_list[MAX_NUM_ATTRIBUTES];

	char* attr_tok = strtok_r(attributes, ",", &ptr1);
	while(attr_tok != NULL) {
		AttributeSchema* cur_attribute = malloc(sizeof(AttributeSchema)); 

		sscanf(attr_tok, " %50s %19s %19[^,]", name, type, constraints);

		strcpy(cur_attribute->name, name);
		strcpy(cur_attribute->type, type);

		// TODO implement logic for type of attribute

		char* const_tok = strtok_r(constraints, " ", &ptr2);
		while(const_tok != NULL) {
			if(strcmp(const_tok, "notnull") == 0) {
				cur_attribute->nonNull = true;
			} else if (strcmp(const_tok, "primaryKey") == 0) {
				cur_attribute->primaryKey = true;
			} else if(strcmp(const_tok, "unique") == 0) {
				cur_attribute->unique = true;
			} else { printf("Constraint %s does not exist", const_tok) }
			const_tok = strtok_r(NULL, " ", &ptr2);
		}

		attribute_list[attribute_count] = cur_attribute;
		attribute_count++;
		attr_tok = strtok_r(NULL, ",", &ptr1);
	}

	return attribute_list;
}

TableSchema* ParseTable(char* table_name, char* attributes) {
	int attributeCount = 0;
	AttributeSchema attributes_parsed[MAX_NUM_ATTRIBUTES];

	TableSchema* table = malloc(sizeof(table));
	initializeTable(table);

	// sets thename of the table
	strcpy(table->name, table_name);

	// TODO implenment parsing for attributes
	// Can be taken from main parse

	return NULL;
}


void parse() {
	// to hold information parsed from stdin
	char command[10];
	char table_name[50];
	char attributes[500];
		
	// if something is parsed, continue
	if(scanf("%9s", command) == 1) {
		// case: create table
		// TODO make this work with the storage manager
		if(strcmp(command, "create") == 0) {
			scanf(" table %49[^(](%99[^)])", table_name, attributes); 
			printf("tablename: %s\n", table_name);
			
			TableSchema* table = Malloc(sizeof(table));
			table = ParseTable(table_name, attributes);

			char *tok = strtok(attributes, ",");
			while (tok != NULL) {
				sscanf(tok, " %49s %19s %19[^,]",
					attrList[attributeCount].attributeName,
                   			attrList[attributeCount].attributeType,
                  			attrList[attributeCount].constraints);
				attributeCount++;
				tok = strtok(NULL, ",");
			
			}
		
		// case: drop table
		} else if(strcmp(command, "drop") == 0) {
			scanf(" table %49s", table_name);
			// call table drop method here
			printf("drop table: %s\n", table_name);

		// case: alter table
		// TODO implement this
		} else if(strcmp(command, "alter") == 0) {
			char opt[50];
			scanf(" table %49s %9s %49[^;];", table_name, command, opt);
			printf("name: %s\ncommand: %s\noptions: %s\n", table_name, command, opt);
			
			if(strcmp(command, "drop") == 0) {
				// drop attribute with name opt
				printf("dropped attribute");

			} else if(strcmp(command, "add") == 0) {
				// TODO implement add attribute
			}

		} else if(strcmp(command, "insert") == 0) {
			// parses tablename and attributes out of command
			scanf(" into %49s values %99[^;]s;", table_name, attributes);
			
			// tokenizes the input tuples
			char *tok = strtok(attributes, ",");
			while (tok != NULL) {
				// TODO process and create records here
				tok = strtok(NULL, ",");
			}

		} else if(strcmp(command, "display") == 0) {
			// TODO implement display DDL

		} else if(strcmp(command, "select") == 0) {
			// TODO implement select DDL
	
		} else{
			printf("unknown command");
		}

	}




}

int main(int arc, char** argv) {
	parse();
	return 0;

}



