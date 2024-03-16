import java.io.IOException;
import java.lang.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            if (attr.gettype().equalsIgnoreCase("varchar")) {
                String attrValue = (String)tuple.get(tupleIndex);
                recordString.append(String.format(" %-10s |", attrValue));
            }
            else if (attr.gettype().equalsIgnoreCase("char")) {
                String attrValue = (String)tuple.get(tupleIndex);
                recordString.append(String.format(" %-10s |", attrValue));
            }
            else if (attr.gettype().equalsIgnoreCase("integer")) {
                int attrValue = (int)tuple.get(tupleIndex);
                recordString.append(String.format(" %-10s |", attrValue));
            }
            else if (attr.gettype().equalsIgnoreCase("double")) {
                double attrValue = (double)tuple.get(tupleIndex);
                recordString.append(String.format(" %-10s |", attrValue));
            }
            else if (attr.gettype().equalsIgnoreCase("boolean")) {
                boolean attrValue = (boolean)tuple.get(tupleIndex);
                recordString.append(String.format(" %-10s |", attrValue));
            }
            tupleIndex++;
        }
        System.out.println(recordString);
    }
    
    private static void handleCreateCommand(String inputLine, Catalog catalog) {
        String[] parts = inputLine.split("\\s+", 4);
        if (parts.length < 4 || !parts[0].equalsIgnoreCase("create") || !parts[1].equalsIgnoreCase("table")) {
            System.out.println("Syntax error in CREATE TABLE command.");
            return;
        }
        String tableName = parts[2];
        String attributesLine = parts[3].trim();
        if (!attributesLine.endsWith(");")) {
            System.out.println("Expected ');' at the end of the CREATE TABLE command.");
            return;
        }
        // Removing '(', ');', and splitting by ','
        String[] attributeTokens = attributesLine.substring(1, attributesLine.length() - 2).split(",");
        ArrayList<AttributeSchema> attributes = new ArrayList<>();
        for (String token : attributeTokens) {
            attributes.add(AttributeSchema.parse(token.trim())); // Assuming AttributeSchema.parse() method exists
        }
        TableSchema table = new TableSchema(attributes.size(), tableName, catalog.getNextTableNumber(), attributes.toArray(new AttributeSchema[0]));
        catalog.addTable(table);
        System.out.println("Table " + tableName + " created successfully.");
    }
    
    private static void handleDropCommand(String inputLine, Catalog catalog) {
        String[] parts = inputLine.split("\\s+");
        if (parts.length != 3 || !parts[0].equalsIgnoreCase("drop") || !parts[1].equalsIgnoreCase("table")) {
            System.out.println("Syntax error in DROP TABLE command.");
            return;
        }
        String tableName = parts[2].replace(";", "");
        catalog.dropTable(tableName);
        System.out.println("Table " + tableName + " dropped successfully.");
    }
    
    private static void handleAlterCommand(String inputLine, Catalog catalog) {
        // This is a simplified version. You might need to expand it based on your ALTER TABLE needs.
        String[] parts = inputLine.split("\\s+", 5);
        if (parts.length < 5 || !parts[0].equalsIgnoreCase("alter") || !parts[1].equalsIgnoreCase("table")) {
            System.out.println("Syntax error in ALTER TABLE command.");
            return;
        }
        String tableName = parts[2];
        TableSchema table = catalog.getTableSchemaByName(tableName); // Assuming such method exists
        if (table == null) {
            System.out.println("Table " + tableName + " not found.");
            return;
        }
        String operation = parts[3]; // "add", "drop", etc.
        String definition = parts[4].replace(";", "");
    
        switch (operation.toLowerCase()) {
            case "add":
                AttributeSchema newAttr = AttributeSchema.parse(definition); // Assuming AttributeSchema.parse() method exists
                table.addAttribute(newAttr);
                System.out.println("Attribute " + newAttr.getname() + " added to table " + tableName + ".");
                break;
            case "drop":
                // Assuming table has a method to drop an attribute
                table.dropAttribute(definition);
                System.out.println("Attribute " + definition + " dropped from table " + tableName + ".");
                break;
            default:
                System.out.println("Unsupported ALTER TABLE operation: " + operation);
        }
    }

    private static void handleInsertCommand(String inputLine, Catalog c, StorageManager storageManager) {
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
            List<TableSchema> tables = c.getTables(); // Get the List of tables from the Catalog
            for (int i = 0; i < c.getTableCount(); i++) {  // Check each table in the schema to see if a name matches
                t = tables.get(i); // Use .get(i) to access the TableSchema object
                if (tokens[index].equals(t.getName())) {  // Use .getName() to access the name of the TableSchema
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
                if ("integer".equalsIgnoreCase(a.gettype())) {
                    if (!token.matches("\\d+")) {  // Checks if the token contains only digits
                        System.out.println("Expected an integer");
                        return;
                    }
                    valueSizes[i] = Integer.SIZE / 8;
                    values.add(Integer.parseInt(token));
                } else if ("double".equalsIgnoreCase(a.gettype())) {
                    try {
                        double doubleValue = Double.parseDouble(token);
                        valueSizes[i] = Double.SIZE / 8;
                        values.add(doubleValue);
                    } catch (NumberFormatException e) {
                        System.out.println("Expected a double");
                        return;
                    }
                } else if ("boolean".equalsIgnoreCase(a.gettype())) {
                    if (!"true".equalsIgnoreCase(token) && !"false".equalsIgnoreCase(token)) {
                        System.out.println("Expected a boolean");
                        return;
                    }
                    valueSizes[i] = Byte.SIZE / 8;
                    values.add(Boolean.parseBoolean(token));
                } else if ("char".equalsIgnoreCase(a.gettype())) {
                    System.out.println("tokLength: " + token.length());
                    if (token.charAt(0) != '"' || token.length() - 1 != a.getsize() || token.charAt(a.getsize() + 1) != '"') {
                        System.out.println("Expected quotes around char or incorrect size of char");
                        return;
                    }
                    valueSizes[i] = a.getsize();
                    values.add(token);
                } else if ("varchar".equalsIgnoreCase(a.gettype())) {
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
            storageManager.addRecord(c, r, t.gettableNumber());

            index++;  // Go to the next tuple
        }
    }

    private static void intToBytes(int i, Object getdata, int ptrIndex) {
        // TODO: Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'intToBytes'");
    }

    private static void displaySchema(Catalog catalog) {
        // Assuming the Catalog class has methods to retrieve DB directory, page size, buffer size
        System.out.println("\nDB location: " + catalog.getDbDirectory());
        System.out.println("Page Size: " + catalog.getPageSize());
        System.out.println("Buffer Size: " + catalog.getBufferSize() + "\n");
    
        List<TableSchema> tables = catalog.getTables(); // Get the List of tables
        if (tables.isEmpty()) {
            System.out.println("No tables to display");
        } else {
            System.out.println("Tables:\n");
            for (TableSchema table : tables) {
                System.out.println("Table name: " + table.getName()); // Assuming there's a getName() method in TableSchema
                System.out.println("Table schema:");
                AttributeSchema[] attributes = table.getattributes(); // Assuming there's a getAttributes() method returning an array
                for (AttributeSchema attr : attributes) {
                    String attributeDetails = String.format("    %s:%s", attr.getname(), attr.gettype()); // Assuming getters for name and type
                    if (attr.isPrimaryKey()) attributeDetails += " primarykey";
                    if (attr.isUnique()) attributeDetails += " unique";
                    if (attr.isNonNull()) attributeDetails += " notnull";
                    System.out.println(attributeDetails);
                }
                // Assuming TableSchema class has methods to retrieve the number of pages and records
                System.out.println("Pages: " + table.getNumPages());
                System.out.println("Records: " + table.getNumRecords() + "\n");
            }
        }
        System.out.println("SUCCESS");
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
                handleInsertCommand(inputLine, catalog, storageManager);
                break;

            case "select":
                handleSelectCommand(inputLine, catalog, storageManager);
                break;

            case "display":
                if (tokens.length > 1 && tokens[1].equalsIgnoreCase("info")) {
                    // Display info about a specific table
                    if (tokens.length > 2) {
                        String tableName = tokens[2].replaceAll(";", "");
                        if (!catalog.tableExists(tableName)) {
                            System.out.println("No such table " + tableName);
                            System.out.println("ERROR");
                        } else {
                            // Table exists, find and display its info
                            boolean found = catalog.findTableDisplay(tableName);
                            if (found) {
                                System.out.println("SUCCESS");
                            } else {
                                // This case may not be necessary, as findTableDisplay already checks for existence
                                System.out.println("ERROR");
                            }
                        }
                    }
                } else {
                    // If the command is just "display schema;", handle it as before
                    displaySchema(catalog);
                }
                break;

            default:
                System.out.println("Unknown command: " + command);
        }
    }
}