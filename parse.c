#include <stdio.h>
#include <stdlib.h>
#include <string.h>
// Define a struct to store the parsed information
struct Query {
    char command[20];
    char args[10][20];
    int num_args;
};

int main() {
    struct Query query;
    query.num_args = 0;
    char arg[30];

    int result = fscanf(stdin, "%19s table %19s", query.command, arg);

    if (result == 2) {
	char* token = strtok(arg, ",");
	while(token != NULL) {
	    strcpy(query.args[query.num_args], token);
	    query.num_args++;

	    token = strtok(NULL, ",");
	}
	printf("Command: %s\n", query.command);

        // Printing the list of arguments
        printf("Argument List: ");
        for (int i = 0; i < query.num_args; i++) {
            printf("%s ", query.args[i]);
        }
        printf("\n");
    } else {
        fprintf(stderr, "Error parsing input\n");
    }

    return 0;
}

