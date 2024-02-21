#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "parse.h"
#include "main.h"
#include "attribute.h"
#include "table.h"
#include "catalog.h"

// to hold information parsed from stdin
char command[10];
char table_name[50];
char attributes[500];

/*	
* Method: ParseAttribute
* Helper function to parse attribute input
*
* @param char* attributes: string consisting of attribute input
* 							Ex: (foo int primarykey unique, bar string notnull)
* @return returns a list of attribute schemas 
*/
AttributeSchema* ParseAttribute(char* attributes) {
	int attribute_count = 0;
	char *ptr1, *ptr2;
	// holds name of attribute
	char* name[MAX_NAME_SIZE];
	// holds type of attribute
	char* type[MAX_NAME_SIZE];
	// size of type
	int size = 0;
	// holds contraints, this must be PARSED 
	char* constraints[MAX_NAME_SIZE];
	// list of attributes to return
	AttributeSchema* attribute_arr[MAX_NUM_ATTRIBUTES];

	// uses strtok_r so for nested parsing 
	char* attr_tok = strtok_r(attributes, ",", &ptr1);
	while(attr_tok != NULL) {
		// current attribute being parsed
		AttributeSchema* cur_attribute = malloc(sizeof(AttributeSchema)); 

		// parses name, type, and constraints 
		sscanf(attr_tok, " %50s %19s %19[^,]", name, type, constraints);

		// puts name and type into attribute
		strcpy(cur_attribute->name, name);
		strcpy(cur_attribute->type, type);

		// for type size
		if(strcmp(type, "integer") == 0) {
			size = 4;
		} else if(strcmp(type, "double") == 0) {
			size = 8;
		} else if(srcmp(type, "boolean") == 0) {
			size = 1;
		} else {
			int size;
			scanf(type, "%10s[^(](%d)", type, size);
			// TODO unfinished
		}

		// tokenizes constraints
		char* const_tok = strtok_r(constraints, " ", &ptr2);
		// loops through constraints, flips constraint flag if constraint found
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

		// adds attribute to array
		attribute_arr[attribute_count] = cur_attribute;
		// increments count
		attribute_count++;
		// gets next attribute
		attr_tok = strtok_r(NULL, ",", &ptr1);
	}

	return attribute_arr;
}

/*
* Method: ParseTable
* Helper function to parse table info
*
* @return returns a table struct
*/
TableSchema* ParseTable() {
	// allocates size of a table
	TableSchema* table = Malloc(sizeof(table));
	initializeTable(table);

	// parses name of table and attributes
	scanf(" table %49[^(](%99[^)])", table_name, attributes); 

	// set the name of the table
	strcpy(table->name, table_name);

	// parses attributes
	AttributeSchema * attributes_parsed = malloc(sizeof(AttributeSchema) * MAX_NUM_ATTRIBUTES);
	attributes_parsed = ParseAttribute(attributes);
	table->attributes = attributes_parsed;


	return table;
}


void parse() {
	Catalog* catalog = getCatalog();
		
	// if something is parsed, continue
	if(scanf("%9s", command) == 1) {
		// case: create table
		if(strcmp(command, "create") == 0) {
			TableSchema* table = ParseTable();
			// TODO send this somewhere
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



