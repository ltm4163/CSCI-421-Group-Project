#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// temporary struct definition. This can be removed once the catalog is working.
// TODO the code will require modifications once catalog and storage manager are working.
typedef struct {
	char attributeName[50];
	char attributeType[20];
	char constraints[20];

} Attribute;


void parse(FILE *s) {
	// to hold information parsed from stdin
	char command[10];
	char tableName[50];
	char attributes[500];
	
	// if something is parsed, continue
	if(scanf("%9s", command) == 1) {
		// case: create table
		// TODO make this work with the storage manager
		if(strcmp(command, "create") == 0) {
			scanf(" table %49[^(](%99[^)])", tableName, attributes); 
			printf("tablename: %s\n", tableName);
			
			int attributeCount = 0;
			Attribute attrList[50];
			
			char *tok = strtok(attributes, ",");
			while (tok != NULL) {
				sscanf(tok, " %49s %19s %19[^,]",
					attrList[attributeCount].attributeName,
                   			attrList[attributeCount].attributeType,
                  			attrList[attributeCount].constraints);
				attributeCount++;
				tok = strtok(NULL, ",");
			
			}

			for (int i = 0; i < attributeCount; i++) {
           			 printf("Attribute %d:\n", i + 1);
            			 printf("  Name: %s\n", attrList[i].attributeName);
            			 printf("  Type: %s\n", attrList[i].attributeType);
            			 printf("  Constraint: %s\n", attrList[i].constraints);
       			 }
		
		// case: drop table
		// TODO implement this
		} else if(strcmp(command, "drop") == 0) {

			printf("drop here");

		// case: alter table
		// TODO implement this
		} else if(strcmp(command, "alter") == 0) {
			printf("alter here");

		} else {
			printf("unknown command");
		}

	}




}

int main(int argc, char** argv) {
	parse(stdin);
	return 0;
}

