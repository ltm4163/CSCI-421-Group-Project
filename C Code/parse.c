#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include "buffer.h"
#include "parse.h"
#include "attribute.h"
#include "table.h"
#include "catalog.h"
#include "main.h"
#include "errno.h"
#include "storagemanager.h"

// to hold information parsed from stdin
char command[10];
char table_name[MAX_NAME_SIZE];
char attributes[500];
int num_attributes;
AttributeSchema attribute_arr[MAX_NUM_ATTRIBUTES];

void removeFirstCharacter(char* str) {
    if (str != NULL && *str != '\0') {
        // Move the pointer to the next character in the string
        memmove(str, str + 1, strlen(str));
    }
}

int containsOnlyDigits(const char *str) {
    while (*str) {
        if (!isdigit(*str)) {
            return 0; // Not a digit
        }
        str++;
    }
    return 1; // Contains only digits
}

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
    constraints[0] = '\0';
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
        // TODO fix type parsing
		sscanf(attr_tok, " %s %s %[^,);]", name, type, constraints);
		// for type size
        sscanf(type, "%19[^(](%d", type, size);
        if(strcmp(type, "integer") == 0) {
            size = 4;
        } else if(strcmp(type, "double") == 0) {
            size = 8;
        } else if(strcmp(type, "boolean") == 0) {
            size = 1;
        } else {
            sscanf(type, "%10s[^(](%d)[^);]", type, size);
            // TODO unfinished
        }

        if(strlen(constraints) > 0) {
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
TableSchema* ParseTable(char* tableName, char* attributes) {

    TableSchema* table = malloc(sizeof(TableSchema));
    if (!table) {
        fprintf(stderr, "Memory allocation failed\n");
        return NULL;
    }
    
    ParseAttribute(attributes);
    initializeTable(table, num_attributes, tableName, attribute_arr);

    return table;
}

void handleCreateCommand(char* inputLine) {
    Catalog* catalog = getCatalog();
    // Find the position of the first '(' which marks the start of attributes
    char* startPos = strchr(inputLine, '(');
    
    if (startPos != NULL && startPos - inputLine < MAX_NAME_SIZE) {
        // Extract table name
        char tableName[MAX_NAME_SIZE] = {0};
        strncpy(tableName, inputLine + strlen("create table "), startPos - (inputLine + strlen("create table ")));
        if(tableExists(catalog, tableName)) {
            printf("Table of name %s already exists\nERROR\n\n", tableName);
            return;
        }
        for (int i = strlen(tableName) - 1; i >= 0 && isspace((unsigned char)tableName[i]); i--) {
            tableName[i] = '\0';
        }

        // Get other attributes
        char attributes[500] = {0}; // Ensure this buffer is sufficiently large
        strncpy(attributes, startPos+1, sizeof(attributes) - 1);

        TableSchema* table = ParseTable(tableName, attributes);
        if (table != NULL && hasPrimaryKey(table)) {
            addTable(catalog, table);
            printf("SUCCESS\n\n", tableName);
        } else if (!hasPrimaryKey(table)) {
            printf("No primary key defined\nFAILURE\n\n");
        } else {
            printf("Failed to create table '%s'.\n", tableName);
        }
    } else {
        printf("Invalid create table command.\nERROR\n\n");
    }
}




void displaySchema(Catalog* catalog) {
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
    printf("\nDB location: %s \n", getDbDirectory());
    printf("Page Size: %d\n", getPageSize());
    printf("Buffer Size: %d \n\n", getBufferSize());
    printf("Tables:\n");
    displayCatalog(catalog);
}

// Find the correct table in the catalog and print it's info

void handleAlterCommand(char* inputLine) {
    //            char opt[50];
    //            scanf(" table %49s %9s %49[^;];", table_name, command, opt);
    //            printf("name: %s\ncommand: %s\noptions: %s\n", table_name, command, opt);
    //
    //            if(strcmp(command, "drop") == 0) {
    //                // TODO: implement drop attribute
    //                printf("SUCCESS\n");
    //
    //            } else if(strcmp(command, "add") == 0) {
    //                // TODO: implement add attribute
    //            }
}

void handleDropCommand(char* inputLine) {
    char tableName[MAX_NAME_SIZE];
    if (sscanf(inputLine, "drop table %s", tableName) == 1) {
        dropTable(getCatalog(), tableName);
        
        // Delete associated file(s)
        char filename[256];
        snprintf(filename, sizeof(filename), "tables/%s.bin", tableName);
        if (remove(filename) == 0) {
            printf("Table '%s' and its data have been successfully deleted.\n", tableName);
        } else {
            perror("Error deleting table file");
        }
        
        // Remove from buffer
        clearTablePagesFromBuffer(getBuffer(), tableName);
    } else {
        printf("Invalid command or table name.\n");
    }
}


// TODO: Need to make sure this works when tuples aren't separated by spaces and just commas
// Must deal with Integer, Double, Boolean, Char(x), Varchar(x)
void handleInsertCommand(char* inputLine) {
    Catalog* c = getCatalog();
    TableSchema* t;
    AttributeSchema* a;

    char* semiColonCheck = strchr(inputLine, ';');  // Creates a string starting at the position of the first instance of a semicolon

    if (semiColonCheck == NULL) {  // If there are no semicolons...
        printf("Expected ';'");
        return;
    }
    else if (strcmp(semiColonCheck, ";")) {  // If the semicolon's position is not at the end of the command...
        printf("';' expected at the end of the statement");
        return;
    }

    char* valuesStart = strstr(inputLine, "values") + 7;  // Creates a string starting at the end of values and the start of the tuples
                                                          //  Will be used later...

    char* inputLineArray = (char*)malloc(strlen(inputLine) + 1);  // The +1 allocates space for the null terminator
    strcpy(inputLineArray, inputLine);  // strtok doesn't work unless you use a char array
    char* token = strtok(inputLineArray, " ");  // Tokenizes the input string

    token = strtok(NULL, " ");  // Continues to the next token; we already checked for insert

    if (token == NULL || strcmp(token, "into")) {  // Checks if 'into' is the next token
        printf("Expected 'into'");
        return;
    }

    token = strtok(NULL, " ");  // Continues to the next token

    if (token != NULL) {  // Make sure there is a table name
    bool found = false;  // Flag to indicate whether we have found the table or not
        for (int i = 0; i < c -> tableCount; i++) {  // Check each table in the schema to see if a name matches
            t = &c -> tables[i];
            if (!strcmp(token, t -> name)) {  // If the token is equal to the current table's name...
                found = true;
                break;
            }
        }
        if (!found) {
            printf("Table not found");
            return;
        }
    }

    token = strtok(NULL, " ");

    if (token == NULL || strcmp(token, "values")) {
        printf("Expected 'values'");
        return;
    }

    token = strtok(NULL, " ");

    // Parse the attributes
    while (token != NULL) {
        void** values = (void**)malloc(t -> numAttributes * sizeof(void*));
        int* valueSizes = (int*)malloc(t -> numAttributes * sizeof(int));

        char firstChar = *token;  // Get the first character of the token (should be a '(' )

        if (token == NULL || !firstChar == '(') {
            printf("Expected '('");
            return;
        }

        removeFirstCharacter(token);  // Removes the '('; always updates token to include the first attribute

        for (int i = 0; i < t -> numAttributes; i++) {
            bool last = false;  // A flag to check whether this current attribute is the last in the tuple
            char truncatedToken[strlen(token)];  // To be used if the token contains the last attribute in a tuple
            a = &t -> attributes[i];  // Grab the i'th attribute

            // Check if the current attribute is the last attribute
            if (token[strlen(token) - 2] == ')' && (token[strlen(token) - 1] == ';') || token[strlen(token) - 1] == ',') {
                strncpy(truncatedToken, token, strlen(token) - 2);  // Remove the parenthesis and comma or semicolon from the attribute
                truncatedToken[strlen(token) - 2] = '\0';  // Add null terminator
                last = true;
            }
            else {
                strncpy(truncatedToken, token, strlen(token));
            }

            // Check if the type matches the current attribute
            if (!strcmp(a -> type, "integer")) {
                if (!containsOnlyDigits(truncatedToken)) {
                    printf("Expected an integer");
                    return;
                }
                valueSizes[i] = sizeof(int);
                values[i] = malloc(valueSizes[i]);
                sscanf(truncatedToken, "%d", (int*)values[i]);
            }
            else if (!strcmp(a -> type, "double")) {
                char *endptr;
                strtod(truncatedToken, &endptr);
                if (!(*truncatedToken != '\0' && *endptr == '\0' && endptr != truncatedToken)) {
                    printf("Expected a double");
                    return;
                }
                valueSizes[i] = sizeof(double);
                values[i] = malloc(valueSizes[i]);
                sscanf(truncatedToken, "%lf", (double*)values[i]);
            }
            else if (!strcmp(a -> type, "boolean")) {
                if(!(strcasecmp(truncatedToken, "true") == 0 || strcasecmp(truncatedToken, "false") == 0)) {
                    printf("Expected a boolean");
                    return;
                }
                valueSizes[i] = sizeof(bool);
                values[i] = malloc(valueSizes[i]);
                sscanf(truncatedToken, "%d", (bool*)values[i]);
            }
            else if (!strcmp(a -> type, "char")) {
                if (truncatedToken[0] != '"') {
                    printf("Expected quotes around char");
                    return;
                }
                removeFirstCharacter(truncatedToken);  // Removes the first quotation
                if (strlen(truncatedToken) - 1 != a -> size) {
                    printf("Incorrect size of char");
                    return;
                }
                if (truncatedToken[a -> size] != '"') {
                    printf("Expected quotes around char");
                    return;
                }
            }
            else if (!strcmp(a -> type, "varchar")) {
                if (truncatedToken[0] != '"') {
                    printf("Expected quotes around varchar");
                    return;
                }
                removeFirstCharacter(truncatedToken);  // Removes the first quotation
                if (strlen(truncatedToken) - 1 > a -> size) {
                    printf("Varchar is over the size limit");
                    return;
                }
                if (truncatedToken[a -> size] != '"') {
                    printf("Expected quotes around varchar");
                    return;
                }
            }

            if (a -> unique) {
                // Need to sift through all records and make sure the current data (truncatedToken) is not the same as an existing data

                // Buffer *bPool;
                // Page *pages = (Page*)malloc(sizeof(Page) * t -> numPages); //locally store pages
                // bool *pagesInBuf = (bool*)malloc(sizeof(bool) * t -> numPages); //keep track of which pages are in buffer
                // memset(pagesInBuf, false, t -> numPages); //initialize pagesInBuf values to false
                
                // for(int i = 0; i < buf_size(bPool); i++) { //find table's pages in buffer; assumes the buffer is initialized...
                //     Page *pg=(Page *)malloc(sizeof(Page));
                //     buf_get(bPool, pg);
                //     if (pg->tableNumber != t -> tableNumber) { //skip page if not in desired table
                //         continue;
                //     }
                //     pages[pg->pageNumber] = *pg;
                //     pagesInBuf[pg->pageNumber] = true; //if page in buffer, set corresponding value in pagesInBuf to true
                // }
            }

            if (a -> nonNull) {
                if (!strcmp(truncatedToken, "null")) {
                    printf("Attribute should not be null");
                    return;
                }
            }

            if (a -> primaryKey) {
                if (!strcmp(truncatedToken, "null")) {
                    printf("Attribute should not be null");
                    return;
                }
            }

            if (!last) {
                token = strtok(NULL, " ");  // Go to next tuple
            }
        }

        Record* r = (Record*)malloc(sizeof(Record));  // Record to insert
        r -> size = t -> numAttributes * sizeof(void*) + t -> numAttributes * sizeof(int);
        r -> data = malloc(r -> size);

        char* ptr = (char*)r -> data;  // Pointer to the record's data
        memcpy(ptr, values, t -> numAttributes * sizeof(void*));
        ptr += t -> numAttributes * sizeof(void*);
        memcpy(ptr, valueSizes, t -> numAttributes * sizeof(int));

        void** valuesTest = (void**)r -> data;
        int* valueSizesTest = (int*)((char*)r -> data + r -> size - sizeof(int) * r -> size / sizeof(void*));

        for (int i = 0; i < t -> numAttributes; i++) {
            if (i > 0) {
            printf(", ");
            }

            if (valueSizes[i] == sizeof(int)) {
                printf("%d", *((int*)valuesTest[i]));
            }
        }

        //addRecord(c, r, t -> tableNumber);  // insert functionality; uncomment this method when implemented

        token = strtok(NULL, " ");  // Go to the next tuple
    }
}


/// @brief Parses the select command and calls a method to print the requested contents (NOTE: only works with 'select * from [tableName];)
/// @param inputLine The input from the user (assumed to include the 'select' keyword)
void handleSelectCommand(char* inputLine) {
    Catalog* c = getCatalog();

    char* inputLineArray = (char*)malloc(strlen(inputLine) + 1);  // The +1 allocates space for the null terminator
    strcpy(inputLineArray, inputLine);  // strtok doesn't work unless you use a char array
    char* token = strtok(inputLineArray, " ");  // Tokenizes the input string

    token = strtok(NULL, " ");  // Continues to the next token; we already checked for select

    if (token == NULL || strcmp(token, "*")) {  // Checks if the current token is equal to "*"
        printf("Expected '*'");
        return;
    }

    token = strtok(NULL, " ");  // Continues to the next token

    if (token == NULL || strcmp(token, "from")) {
        printf("Expected 'from'");
        return;
    }

    token = strtok(NULL, " ");

    if (token != NULL) {  // Make sure there is a table name
        for (int i = 0; i < c -> tableCount; i++) {  // Check each table in the schema to see if a name matches
            TableSchema* t = &c -> tables[i];
            token[strlen(token) - 2] = '\0';  // Strips the table name of the semicolon at the end for comparison
            if (!strcmp(token, t -> name)) {  // If the token is equal to the current table's name...
                token = strtok(NULL, " ");
                getRecords(t -> tableNumber);  // Select's functionality
                return;
            }
        }
    }
    else {  // If the table name is not present in the query...
        printf("Expected table name");
        return;
    }
    // TODO get this to display tablename 
    // should be: no such table [tablename here]
    printf("no such table\nERROR\n\n");  // If this code is reached, a table with a matching name was not found
    return;
}

// TODO: Does each line of input need a ';' to be valid?? -- Yes
int parse(char* inputLine, char* dbpath) {
    Catalog* catalog = getCatalog();
    char command[10];
    char nextWord[100];
    char* semiColonCheck = strchr(inputLine, ';');  // Creates a string starting at the position of the first instance of a semicolon

    if (semiColonCheck == NULL && strcmp(inputLine, "<quit>") != 10) {  // If there are no semicolons...
        printf("Expected ';'\n");
        return 0;
    }

    if (sscanf(inputLine, "%9s", command) > 0) {
        // Quit
        if (strcmp(command, "<quit>") == 0) {
            printf("\nSafely shutting down the database...\n");
            printf("Purging page buffer...\n");
            
            writeBufferToHardware(getBuffer());
            writeCatalogToFile(catalog, dbpath);
    
            printf("Exiting the database...\n\n");
            
            return 1; // 1 = TRUE = EXIT CLI loop in main()
        }
        
        // Create table
        if (strcmp(command, "create") == 0) {
            handleCreateCommand(inputLine);
        }
        // Drop table
        else if (strcmp(command, "drop") == 0) {
            handleDropCommand(inputLine);
        }
        // Alter table
        else if (strcmp(command, "alter") == 0) {
            // This may not be needed for this phase???
            handleAlterCommand(inputLine);
        }
        // Insert
        else if (strcmp(command, "insert") == 0) {
            handleInsertCommand(inputLine);
        }
        // Display
        else if (strcmp(command, "display") == 0) {
            if (sscanf(inputLine, "display %100[^ ;]", nextWord) == 1) {
                if (strcmp(nextWord, "schema") == 0) {
                    displaySchema(catalog);
                    printf("SUCCESS\n\n");
                    return 0;
                }
                else if (strcmp(nextWord, "info") == 0) {
                    char tableName[MAX_NAME_SIZE];
                    if (sscanf(inputLine, "display info %99[^;];", tableName) == 1) {
                        if(!findTableDisplay(catalog, tableName)) {
                            printf("no such table %s\n", tableName);
                            printf("ERROR\n\n");
                        } else { printf("SUCCESS\n\n"); }
                        return 0;
                    }
                }
            }
            return 0;
        }
        // Select
        else if (strcmp(command, "select") == 0) {
            handleSelectCommand(inputLine);
        } else {
            printf("Unknown command\n");
        }

        return 0;
    }

    return 0;
}
