import java.io.IOException;
import java.lang.*;
import java.util.ArrayList;

public class parser {

    public static void printAttributeNames(AttributeSchema[] attributeSchemas) {
        StringBuilder header = new StringBuilder("|");
        StringBuilder separator = new StringBuilder("+");
        for (AttributeSchema attr : attributeSchemas) {
            String attrName = String.format(" %-10s |", attr.getname());
            header.append(attrName);
            separator.append("-".repeat(attrName.length() - 1)).append("+");
        }
        System.out.println(separator);
        System.out.println(header);
        System.out.println(separator);
    }
    
    public static void printTuple(ArrayList<Object> tuple, AttributeSchema[] attributeSchemas) {
        StringBuilder recordString = new StringBuilder("|");
        int tupleIndex = 0; // index of current attribute in tuple
        for (AttributeSchema attr : attributeSchemas) {
            
            // print value of each attribute in record
            if (attr.gettype().equals("varchar")) {
                String attrValue = (String)tuple.get(tupleIndex);
                recordString.append(String.format(" %-10s |", attrValue));
            }
            else if (attr.gettype().equals("char")) {
                String attrValue = (String)tuple.get(tupleIndex);
                recordString.append(String.format(" %-10s |", attrValue));
            }
            else if (attr.gettype().equals("integer")) {
                int attrValue = (int)tuple.get(tupleIndex);
                recordString.append(String.format(" %-10s |", attrValue));
            }
            else if (attr.gettype().equals("double")) {
                double attrValue = (double)tuple.get(tupleIndex);
                recordString.append(String.format(" %-10s |", attrValue));
            }
            else if (attr.gettype().equals("boolean")) {
                boolean attrValue = (boolean)tuple.get(tupleIndex);
                recordString.append(String.format(" %-10s |", attrValue));
            }
            tupleIndex++;
        }
        System.out.println(recordString);
    }
    
    private static void handleCreateCommand(String inputLine, Catalog catalog) {
        // TODO: Implement the handleCreateCommand method
    }

    private static void handleDropCommand(String inputLine, Catalog catalog) {
        int indexOfSemicolon = inputLine.indexOf(';');
        if (indexOfSemicolon == -1) {  // If there are no semicolons...
            System.out.println("Expected ';'");
            return;
        } else if (!inputLine.endsWith(";")) {  // If the semicolon's position is not at the end of the statement...
            System.out.println("';' expected at the end of the statement");
            return;
        }
        String []splitfromsemicolon=inputLine.split(";");
        String sqlcommand=splitfromsemicolon[0].trim();
        String [] sqlsplits = sqlcommand.split("\\s+");
        String tablename = sqlsplits[2];
        catalog.dropTable(catalog, tablename);
    }

    private static void handleAlterCommand(String inputLine, Catalog catalog) {
        // TODO: Implement the handleAlterCommand method
    }

    private static void handleInsertCommand(String inputLine, Catalog c) {
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
                if (tokens[index].equals(t.getname())) {  // If the token is equal to the current table's name...
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
            ArrayList<Object> values = new ArrayList<>(t.getnumAttributes());
            int[] valueSizes = new int[t.getnumAttributes()];

            String token = tokens[index];
            char firstChar = token.charAt(0);  // Get the first character of the token (should be a '(')

            if (token == null || firstChar != '(') {
                System.out.println("Expected '('");
                return;
            }

            token = token.substring(1);  // Removes the '('; always updates token to include the first attribute

            for (int i = 0; i < t.getnumAttributes(); i++) {
                boolean last = false;  // A flag to check whether this current attribute is the last in the tuple

                // Check if the current attribute is the last attribute
                if (token.endsWith(");") || token.endsWith(",")) {
                    token = token.substring(0, token.length() - 2);  // Remove the parenthesis and comma or semicolon from the attribute
                    last = true;
                }

                a = t.getattributes()[i];  // Grab the i'th attribute

                // Check if the type matches the current attribute
                if ("integer".equals(a.gettype())) {
                    if (!token.matches("\\d+")) {  // Checks if the token contains only digits
                        System.out.println("Expected an integer");
                        return;
                    }
                    valueSizes[i] = Integer.SIZE / 8;
                    values.add(Integer.parseInt(token));
                } else if ("double".equals(a.gettype())) {
                    try {
                        double doubleValue = Double.parseDouble(token);
                        valueSizes[i] = Double.SIZE / 8;
                        values.add(doubleValue);
                    } catch (NumberFormatException e) {
                        System.out.println("Expected a double");
                        return;
                    }
                } else if ("boolean".equals(a.gettype())) {
                    if (!"true".equalsIgnoreCase(token) && !"false".equalsIgnoreCase(token)) {
                        System.out.println("Expected a boolean");
                        return;
                    }
                    valueSizes[i] = Byte.SIZE / 8;
                    values.add(Boolean.parseBoolean(token));
                } else if ("char".equals(a.gettype())) {
                    if (token.charAt(0) != '"' || token.length() - 1 != a.getsize() || token.charAt(a.getsize() + 1) != '"') {
                        System.out.println("Expected quotes around char or incorrect size of char");
                        return;
                    }
                    valueSizes[i] = a.getsize();
                    values.add(token);
                } else if ("varchar".equals(a.gettype())) {
                    if (token.charAt(0) != '"' || token.length() - 1 > a.getsize() || token.charAt(a.getsize() + 1) != '"') {
                        System.out.println("Expected quotes around varchar or varchar over the size limit");
                        return;
                    }
                    valueSizes[i] = a.getsize();
                    values.add(token);
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

            Record r = new Record(values, t.getnumAttributes() * Integer.BYTES + t.getnumAttributes() * Integer.BYTES);
            // r.setsize(t.getnumAttributes() * Integer.BYTES + t.getnumAttributes() * Integer.BYTES);
            // r.setdata(values);

            int ptrIndex = 0;

            for (int i = 0; i < t.getnumAttributes(); i++) {
                if (i > 0) {
                    System.out.print(", ");
                }

                if (valueSizes[i] == Integer.SIZE / 8) {
                    System.out.print((int) values.get(i));
                    intToBytes((int) values.get(i), r.getdata(), ptrIndex);
                    ptrIndex += Integer.BYTES;
                }
            }

            // Uncomment the following line when the addRecord method is implemented
            // addRecord(c, r, t.getTableNumber());

            index++;  // Go to the next tuple
        }
    }

    private static void intToBytes(int i, Object getdata, int ptrIndex) {
        // TODO: Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'intToBytes'");
    }

    private static void displaySchema(Catalog catalog) {
        // TODO: Implement the displaySchema method
    }

    private static void handleSelectCommand(String inputLine, Catalog c, StorageManager storageManager) {
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
                TableSchema t = c.getTableSchema(i);
                if (tableName.equals(t.getname())) {  // If the token is equal to the current table's name...
                    storageManager.getRecords(t.gettableNumber());
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

    public static void parse(String inputLine, Catalog catalog, PageBuffer buffer, String dbDirectory, int pageSize, StorageManager storageManager) {
        String[] tokens = inputLine.trim().split("\\s+");
        if (tokens.length == 0) {
            System.out.println("No input detected.");
            return;
        }

        String command = tokens[0].toLowerCase();
        
        switch (command) {
            case "quit":
            case "<quit>":
                System.out.println("\nSafely shutting down the database...");
                Main.writeBufferToHardware();
                try {
                    Main.writeCatalogToFile(dbDirectory + "/catalog.bin");
                } catch (IOException e) {
                    System.err.println("Error while saving catalog: " + e.getMessage());
                }
                System.out.println("Exiting the database...\n");
                System.exit(0); // Use System.exit to terminate the application
                break;

            case "create":
                handleCreateCommand(inputLine, catalog);
                break;

            case "drop":
                handleDropCommand(inputLine, catalog);
                break;

            case "alter":
                handleAlterCommand(inputLine, catalog);
                break;

            case "insert":
                handleInsertCommand(inputLine, catalog);
                break;

            case "select":
                handleSelectCommand(inputLine, catalog, storageManager);
                break;

            case "display":
                displaySchema(catalog);
                break;

            default:
                System.out.println("Unknown command: " + command);
        }
    }
}