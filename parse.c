#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "parse.h"
#include "attribute.h"
#include "table.h"
#include "catalog.h"
#include "main.h"

// to hold information parsed from stdin
char command[10];
char table_name[MAX_NAME_SIZE];
char attributes[500];
int num_attributes;
AttributeSchema attribute_arr[MAX_NUM_ATTRIBUTES];

/*	
* Method: ParseAttribute
* Helper function to parse attribute input
*
* @param char* attributes: string consisting of attribute input
* 							Ex: (foo int primarykey unique, bar string notnull)
* @return returns a list of attribute schemas 
*/
void ParseAttribute(char* attributes) {
	int attribute_count = 0;
	char *ptr1, *ptr2;
	// holds name of attribute
	char name[MAX_NAME_SIZE];
	// holds type of attribute
	char type[MAX_NAME_SIZE];
	// size of type
	int size = 0;
	// holds contraints, this must be PARSED 
	char constraints[MAX_NAME_SIZE];
	// list of attributes to return

	// uses strtok_r so for nested parsing 
	char* attr_tok = strtok_r(attributes, ",", &ptr1);
	while(attr_tok != NULL) {
		bool unique = false;
		bool primaryKey = false;
		bool nonNull = false;
		// current attribute being parsed
		AttributeSchema* cur_attribute = malloc(sizeof(AttributeSchema)); 

		// parses name, type, and constraints 
		sscanf(attr_tok, " %50s %19s %19[^,]", name, type, constraints);

		// for type size
		if(strcmp(type, "integer") == 0) {
			size = 4;
		} else if(strcmp(type, "double") == 0) {
			size = 8;
		} else if(strcmp(type, "boolean") == 0) {
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
				nonNull = true;
			} else if (strcmp(const_tok, "primarykey") == 0) {
				primaryKey = true;
			} else if(strcmp(const_tok, "unique") == 0) {
				unique = true;
			} else { printf("Constraint %s does not exist", const_tok); }
			const_tok = strtok_r(NULL, " ", &ptr2);
		}

		initializeAttribute(cur_attribute, name, type, unique, nonNull, primaryKey, size);
		// adds attribute to array
		attribute_arr[attribute_count] = *cur_attribute;
		// increments count
		attribute_count++;
		// gets next attribute
		attr_tok = strtok_r(NULL, ",", &ptr1);
	}
	num_attributes = attribute_count;
}

/*
* Method: ParseTable
* Helper function to parse table info
*
* @return returns a table struct
*/
TableSchema* ParseTable() {
	// allocates size of a table
	TableSchema* table = malloc(sizeof(table));

	// parses name of table and attributes
	scanf(" table %49[^(](%99[^)])", table_name, attributes); 

	// parses attributes
	ParseAttribute(attributes);

	initializeTable(table, num_attributes, table_name, attribute_arr);

	return table;
}

void handleCreateCommand(char* inputLine) {
    // Example for parsing a simplified "create table" command
    char tableName[MAX_NAME_SIZE];
    char attributes[500]; // Adjust size as needed
    
    if (sscanf(inputLine, "create table %s %[^\n]", tableName, attributes) == 2) {
        // Now you have tableName and a string of attributes to be parsed further
        // You can pass these to another function to parse attributes and create the table
//        TableSchema* table = ParseTable();
//        addTable(catalog, table);
    } else {
        printf("Invalid create table command.\n");
        printf("ERROR\n\n");
    }
}


void displaySchema() {
    // Expected output:

    /*
     DB location: /home/csci421/project/db
     Page Size: 4096
     Buffer Size: 10

     No tables to display
     SUCCESS
     */

    /*
     DB location: /home/csci421/project/db
     Page Size: 4096
     Buffer Size: 25

     Tables:

     Table name: foo
     Table schema:
         num:integer primarykey
     Pages: 1
     Records: 3

     Table name: bar
     Table schema:
         x:double primarykey
         y:char(5)
     Pages: 0
     Records: 0
     SUCCESS
     */
    
    // TODO: Store the following as constants and display their values
    printf("\nDB location: \n");
    printf("Page Size: \n");
    printf("Buffer Size: \n");
    
    // TODO: Get all schemas/tables
    printf("\n");
}

void handleDropCommand(char* inputLine) {
    
}

void handleInsertCommand(char* inputLine) {
    
}

void handleSelectCommand(char* inputLine) {
    
}

// TODO: Does each line of input need a ';' to be valid??
int parse(char* inputLine) {
    Catalog* catalog = getCatalog();
    char command[10];

    if (sscanf(inputLine, "%9s", command) > 0) {
        // Quit command
        if (strcmp(command, "<quit>") == 0) {
            printf("\nSafely shutting down the database...\n");
            printf("Purging page buffer...\n");
            printf("Saving catalog...\n\n");
            printf("Exiting the database...\n\n");
            return 1; // 1 = TRUE = EXIT in main()
        }
        
        // Create table command
        if (strcmp(command, "create") == 0) {
            handleCreateCommand(inputLine);
        }
        // Drop table command
        else if (strcmp(command, "drop") == 0) {
            handleDropCommand(inputLine);
        }
        // Insert command
        else if (strcmp(command, "insert") == 0) {
            handleInsertCommand(inputLine);
        }
        // Display command
        else if (strcmp(command, "display") == 0) {
            char nextWord[100];

            if (sscanf(inputLine, "display %s", nextWord) == 1) {
                if (strcmp(nextWord, "schema") == 0) {
                    displaySchema();
                }
            } else {
                displayCatalog(catalog);
            }
        }
        // Select command
        else if (strcmp(command, "select") == 0) {
            handleSelectCommand(inputLine);
        } else {
            printf("Unknown command\n");
        }

        return 0;
    }

    return 0;
}




//int parse() {
//	Catalog* catalog = getCatalog();
//		
//   
//    
//	// if something is parsed, continue
//	if(scanf("%9s", command) == 1) {
//        
//        // Quit
//        if (strcmp(command, "<quit>") == 0) {
//            // TODO: ALL OF THIS!
//            printf("\nSafely shutting down the database...\n");
//            printf("Purging page buffer...\n");
//            printf("Saving catalog...\n\n");
//            printf("Exiting the database...\n\n");
//
//            return 1;
//        }
//        
//		// case: create table
//		if(strcmp(command, "create") == 0) {
//			TableSchema* table = ParseTable();
//			addTable(catalog, table);
//
//		// case: drop table
//		} else if(strcmp(command, "drop") == 0) {
//			scanf(" table %49s", table_name);
//			// call table drop method here
//			dropTable(catalog, table_name);
//
//		// case: alter table
//		// TODO implement this
//		} else if(strcmp(command, "alter") == 0) {
//			char opt[50];
//			scanf(" table %49s %9s %49[^;];", table_name, command, opt);
//			printf("name: %s\ncommand: %s\noptions: %s\n", table_name, command, opt);
//			
//			if(strcmp(command, "drop") == 0) {
//				// drop attribute with name opt
//				printf("dropped attribute");
//
//			} else if(strcmp(command, "add") == 0) {
//				// TODO implement add attribute
//			}
//
//		} 
//        /*
//         BUG FIX: This needs to take in commands that are longer than "insert", such as "insert into foo values (1 "foo");"
//         */
//        else if(strcmp(command, "insert") == 0) {
//			// parses tablename and attributes out of command
//			scanf(" into %49s values %99[^;]s;", table_name, attributes);
//			
//			// tokenizes the input tuples
//			char *tok = strtok(attributes, ",");
//			while (tok != NULL) {
//				// TODO: process and create records here
//                // TODO: Appropriate error handling if the operation fails
//				tok = strtok(NULL, ",");
//			}
//            
//		}
//        /*
//         BUG FIX: This needs to take in commands that are longer than "display", such as "display info foo;"
//         */
//        else if(strcmp(command, "display") == 0) {
//			displayCatalog(catalog);
//
//		} 
//        
//        /*
//         BUG FIX: This needs to take in commands that are longer than "select", such as "select * from foo;"
//         */
//        else if(strcmp(command, "select") == 0) {
//			// TODO: implement select DDL
//            
//            // Show these error statements if select fails
//            printf("No such table <tableName>\n");
//            printf("ERROR\n\n");
//	
//		} else{
//            printf("unknown command\n");
//            printf("ERROR\n\n");
//		}
//        
//        return 0;
//	}
//    return 0;
//}
