import java.lang.*;

import javax.xml.catalog.Catalog;

public class parser {

    private static void handleCreateCommand(String inputLine) {
        // Implement the handleCreateCommand method
    }

    private static void handleDropCommand(String inputLine) {
        // Implement the handleDropCommand method
    }

    private static void handleAlterCommand(String inputLine) {
        // Implement the handleAlterCommand method
    }

    private static void handleInsertCommand(String inputLine) {
        Catalog c = getCatalog();
        TableSchema t = null;
        AttributeSchema a;

        int indexOfSemicolon = inputLine.indexOf(';');

        if (indexOfSemicolon == -1) {  // If there are no semicolons...
            System.out.println("Expected ';'");
            return;
        } else if (!inputLine.endsWith(";")) {  // If the semicolon's position is not at the end of the statement...
            System.out.println("';' expected at the end of the statement");
            return;
        }

        String valuesStart = inputLine.substring(inputLine.indexOf("values") + 7);  // Creates a string starting at the end of values and the start of the tuples

        String[] tokens = inputLine.split(" ");
        int index = 0;

        while (index < tokens.length && !tokens[index].equals("insert")) {
            index++;
        }

        index++; // Move to the next token after 'insert'

        if (index >= tokens.length || !tokens[index].equals("into")) {
            System.out.println("Expected 'into'");
            return;
        }

        index++; // Move to the next token after 'into'

        if (index < tokens.length) {  // Make sure there is a table name
            boolean found = false;  // Flag to indicate whether we have found the table or not
            for (int i = 0; i < c.getTableCount(); i++) {  // Check each table in the schema to see if a name matches
                t = c.getTables()[i];
                if (tokens[index].equals(t.getName())) {  // If the token is equal to the current table's name...
                    found = true;
                    break;
                }
            }
            if (!found || t == null) {
                System.out.println("Table not found");
                return;
            }
        } else {
            System.out.println("Expected table name");
            return;
        }

        index++; // Move to the next token

        if (index >= tokens.length || !tokens[index].equals("values")) {
            System.out.println("Expected 'values'");
            return;
        }

        index++; // Move to the next token

        // Parse the attributes
        while (index < tokens.length) {
            Object[] values = new Object[t.getNumAttributes()];
            int[] valueSizes = new int[t.getNumAttributes()];

            String token = tokens[index];
            char firstChar = token.charAt(0);  // Get the first character of the token (should be a '(')

            if (token == null || firstChar != '(') {
                System.out.println("Expected '('");
                return;
            }

            token = removeFirstCharacter(token);  // Removes the '('; always updates token to include the first attribute

            for (int i = 0; i < t.getNumAttributes(); i++) {
                boolean last = false;  // A flag to check whether this current attribute is the last in the tuple

                // Check if the current attribute is the last attribute
                if (token.endsWith(");") || token.endsWith(",")) {
                    token = token.substring(0, token.length() - 2);  // Remove the parenthesis and comma or semicolon from the attribute
                    last = true;
                }

                a = t.getAttributes()[i];  // Grab the i'th attribute

                // Check if the type matches the current attribute
                if ("integer".equals(a.getType())) {
                    if (!containsOnlyDigits(token)) {
                        System.out.println("Expected an integer");
                        return;
                    }
                    valueSizes[i] = Integer.SIZE / 8;
                    values[i] = Integer.parseInt(token);
                } else if ("double".equals(a.getType())) {
                    try {
                        double doubleValue = Double.parseDouble(token);
                        valueSizes[i] = Double.SIZE / 8;
                        values[i] = doubleValue;
                    } catch (NumberFormatException e) {
                        System.out.println("Expected a double");
                        return;
                    }
                } else if ("boolean".equals(a.getType())) {
                    if (!"true".equalsIgnoreCase(token) && !"false".equalsIgnoreCase(token)) {
                        System.out.println("Expected a boolean");
                        return;
                    }
                    valueSizes[i] = Boolean.SIZE / 8;
                    values[i] = Boolean.parseBoolean(token);
                } else if ("char".equals(a.getType())) {
                    if (token.charAt(0) != '"' || token.length() - 1 != a.getSize() || token.charAt(a.getSize() + 1) != '"') {
                        System.out.println("Expected quotes around char or incorrect size of char");
                        return;
                    }
                } else if ("varchar".equals(a.getType())) {
                    if (token.charAt(0) != '"' || token.length() - 1 > a.getSize() || token.charAt(a.getSize() + 1) != '"') {
                        System.out.println("Expected quotes around varchar or varchar over the size limit");
                        return;
                    }
                }

                if (a.isUnique()) {
                    // Need to sift through all records and make sure the current data (token) is not the same as an existing data
                    // TODO: Implement uniqueness check
                }

                if (a.isNonNull() && "null".equals(token)) {
                    System.out.println("Attribute should not be null");
                    return;
                }

                if (a.isPrimaryKey() && "null".equals(token)) {
                    System.out.println("Attribute should not be null");
                    return;
                }

                if (!last) {
                    index++;  // Go to next tuple
                }
            }

            Record r = new Record();
            r.setSize(t.getNumAttributes() * Integer.BYTES + t.getNumAttributes() * Integer.BYTES);
            r.setData(new byte[r.getSize()]);

            int ptrIndex = 0;

            for (int i = 0; i < t.getNumAttributes(); i++) {
                if (i > 0) {
                    System.out.print(", ");
                }

                if (valueSizes[i] == Integer.SIZE / 8) {
                    System.out.print((int) values[i]);
                    intToBytes((int) values[i], r.getData(), ptrIndex);
                    ptrIndex += Integer.BYTES;
                }
            }

            // Uncomment the following line when the addRecord method is implemented
            // addRecord(c, r, t.getTableNumber());

            index++;  // Go to the next tuple
        }
    }

    private static void displaySchema(Catalog catalog) {
        // Implement the displaySchema method
    }

    private static void handleSelectCommand(String inputLine) {
        Catalog c = getCatalog();

        String[] tokens = inputLine.split(" ");
        int index = 0;

        while (index < tokens.length && !tokens[index].equals("select")) {
            index++;
        }

        index++;  // Move to the next token after 'select'

        if (index >= tokens.length || !tokens[index].equals("*")) {
            System.out.println("Expected '*'\nERROR");
            return;
        }

        index++;  // Move to the next token after '*'

        if (index >= tokens.length || !tokens[index].equals("from")) {
            System.out.println("Expected 'from'\nERROR");
            return;
        }

        index++;  // Move to the next token after 'from'

        if (index < tokens.length) {  // Make sure there is a table name
            String tableName = tokens[index].replaceAll(";", "");  // Strips the table name of the semicolon at the end for comparison

            for (int i = 0; i < c.getTableCount(); i++) {
                TableSchema t = c.getTables()[i];
                if (tableName.equals(t.getName())) {  // If the token is equal to the current table's name...
                    getRecords(t.getTableNumber());
                    return;
                }
            }
        }
        else {  // If the table name is not present in the query...
            System.out.println("Expected table name\nERROR");
            return;
        }

        // If this code is reached, a table with a matching name was not found
        // TODO: get this to display tablename
        // should be: no such table [tablename here]
        System.out.println("No such table\nERROR\n\n");
        return;
    }

    int parse(String inputLine, String dbpath) {
        Catalog catalog = getCatalog();
        String[] tokens = inputLine.split("\\s+");
        String command =  inputTok[0];
        String next;

        if (!inputLine.contains(";") && !inputLine.equals("<quit>")) {
            System.out.println("Expected ';'");
            return 0;
        }

        switch (command) {
            case "<quit>":
                System.out.println("\nSafely shutting down the database...");
                System.out.println("Purging page buffer...");

                writeBufferToHardware(getBuffer());
                writeCatalogToFile(catalog, dbpath);

                System.out.println("Exiting the database...\n");
                return 1; // 1 = TRUE = EXIT CLI loop in main()

            case "create":
                handleCreateCommand(inputLine);
                break;

            case "drop":
                handleDropCommand(inputLine);
                break;

            case "alter":
                // This may not be needed for this phase???
                handleAlterCommand(inputLine);
                break;

            case "insert":
                handleInsertCommand(inputLine);
                break;

            case "display":
                if (tokens.length > 1) {
                    next = tokens[1];
                    if (next.equals("schema")) {
                        displaySchema(catalog);
                        System.out.println("SUCCESS\n");
                        return 0;
                    } else if (next.equals("info") && tokens.length > 2) {
                        String tableName = tokens[2].replace(";", "");
                        if (!findTableDisplay(catalog, tableName)) {
                            System.out.println("no such table " + tableName);
                            System.out.println("ERROR\n");
                        } else {
                            System.out.println("SUCCESS\n");
                        }
                        return 0;
                    }
                }
                return 0;

            case "select":
                handleSelectCommand(inputLine);
                break;

            default:
                System.out.println("Unknown command");
        }




    }
}
