import java.io.IOException;
import java.util.ArrayList;
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
        String[] parts = inputLine.split("\\s+", 3);
        if (parts.length < 3 || !parts[0].equalsIgnoreCase("create") || !parts[1].equalsIgnoreCase("table")) {
            System.out.println("Syntax error in CREATE TABLE command.");
            return;
        }
        String tableNameAttributes = parts[2].trim();
        int tableNameEndIdx = tableNameAttributes.indexOf('(');
        String tableName = tableNameAttributes.substring(0, tableNameEndIdx).trim();
        String attributesLine = tableNameAttributes.substring(tableNameEndIdx).trim();
    
        if (!attributesLine.endsWith(");")) {
            System.out.println("Expected ');' at the end of the CREATE TABLE command.");
            return;
        }
    
        attributesLine = attributesLine.substring(1, attributesLine.length() - 2); // Remove surrounding '()' from attribute definitions
        String[] attributeTokens = attributesLine.split(",\\s*");
        ArrayList<AttributeSchema> attributes = new ArrayList<>();
        for (String token : attributeTokens) {
            attributes.add(AttributeSchema.parse(token.trim()));
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
        TableSchema table = null;
    
        if (!inputLine.endsWith(";")) {
            System.out.println("';' expected at the end of the statement");
            return;
        }
    
        String[] tokens = inputLine.split("\\s+");
        int index = 0;
    
        while (index < tokens.length && !tokens[index].equalsIgnoreCase("insert")) {
            index++;
        }
    
        index++; // Skip "insert"
    
        if (index >= tokens.length || !tokens[index].equalsIgnoreCase("into")) {
            System.out.println("Expected 'into'");
            return;
        }
    
        index++; // Skip "into"
    
        if (index < tokens.length) {
            String tableName = tokens[index];
            table = c.getTableSchemaByName(tableName);
            if (table == null) {
                System.out.println("Table not found: " + tableName);
                return;
            }
        } else {
            System.out.println("Expected table name");
            return;
        }
    
        index++; // Skip table name
    
        if (index >= tokens.length || !tokens[index].equalsIgnoreCase("values")) {
            System.out.println("Expected 'values'");
            return;
        }
    
        index++; // Skip "values"
    
        // Assuming values are directly after "values" keyword and are properly enclosed in parentheses
        String valuesString = inputLine.substring(inputLine.indexOf("(", index)).trim();
        if (!valuesString.endsWith(";")) {
            System.out.println("Expected semicolon at the end of the values");
            return;
        }
        valuesString = valuesString.substring(1, valuesString.length() - 2); // Remove surrounding parentheses and semicolon
    
        // Split values by commas outside of quotes
        String[] valueTokens = valuesString.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    
        if (valueTokens.length != table.getnumAttributes()) {
            System.out.println("Mismatch between number of columns and values provided");
            return;
        }
    
        ArrayList<Object> values = new ArrayList<>();
        AttributeSchema[] attributes = table.getattributes();
    
        for (int i = 0; i < valueTokens.length; i++) {
            String value = valueTokens[i].trim();
            AttributeSchema attribute = attributes[i];
    
            try {
                switch (attribute.gettype().toLowerCase()) {
                    case "integer":
                        values.add(Integer.parseInt(value));
                        break;
                    case "double":
                        values.add(Double.parseDouble(value));
                        break;
                    case "boolean":
                        values.add(Boolean.parseBoolean(value));
                        break;
                    case "char":  // continues to "varchar"
                    case "varchar":
                        if (!value.startsWith("\"") || !value.endsWith("\"")) {
                            throw new IllegalArgumentException("Expected quotes around string value");
                        }
                        // Remove quotes
                        value = value.substring(1, value.length() - 1);
                        values.add(value);
                        break;
                    default:
                        System.out.println("Unsupported attribute type: " + attribute.gettype());
                        return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error parsing value for attribute '" + attribute.getname() + "': " + e.getMessage());
                return;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                return;
            }
        }
    
        Record record = new Record(values, calculateRecordSize(values, attributes));
        storageManager.addRecord(c, record, table.gettableNumber());
    
        System.out.println("Record inserted successfully into table: " + table.getname());
    }
    
    private static int calculateRecordSize(ArrayList<Object> values, AttributeSchema[] attributes) {
        int size = 0;
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            AttributeSchema attr = attributes[i];
    
            switch (attr.gettype().toLowerCase()) {
                case "integer":
                    size += Integer.BYTES; // Integer.SIZE / Byte.SIZE;
                    break;
                case "double":
                    size += Double.BYTES; // Double.SIZE / Byte.SIZE;
                    break;
                case "boolean": // 0 = false, 1 = true
                    size += 1; 
                    break;
                case "char":
                    size += attr.getsize();
                    break;
                case "varchar":
                    // 4 bytes (int) plus the length of the string
                    String stringValue = (String) value;
                    size += Integer.BYTES + stringValue.getBytes().length;
                    break;
                default:
                    System.out.println("Unsupported attribute type: " + attr.gettype());
                    // Consider throwing an exception or handling this case appropriately
                    break;
            }
        }
        return size;
    }

    private static void displaySchema(Catalog catalog) {
        System.out.println("\nDB location: " + catalog.getDbDirectory());
        System.out.println("Page Size: " + catalog.getPageSize());
        System.out.println("Buffer Size: " + catalog.getBufferSize() + "\n");
    
        List<TableSchema> tables = catalog.getTables();
        if (tables.isEmpty()) {
            System.out.println("No tables to display");
        } else {
            System.out.println("Tables:\n");
            for (TableSchema table : tables) {
                System.out.println("Table name: " + table.getName()); 
                System.out.println("Table schema:");
                AttributeSchema[] attributes = table.getattributes(); 
                for (AttributeSchema attr : attributes) {
                    String attributeDetails = String.format("    %s:%s", attr.getname(), attr.gettype());
                    if (attr.isPrimaryKey()) attributeDetails += " primarykey";
                    if (attr.isUnique()) attributeDetails += " unique";
                    if (attr.isNonNull()) attributeDetails += " notnull";
                    System.out.println(attributeDetails);
                }
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
        index++;  // Move past 'select'
        if (index >= tokens.length || !tokens[index].equals("*")) {
            System.out.println("Expected '*' after 'select'\nERROR");
            return;
        }
        index++;  // Move past '*'
        if (index >= tokens.length || !tokens[index].equals("from")) {
            System.out.println("Expected 'from' after '*'\nERROR");
            return;
        }
        index++;  // Move past 'from'
        if (index < tokens.length) {  // Ensure there is a table name
            String tableName = tokens[index].replaceAll(";", "");  // Remove semicolon
            TableSchema t = c.getTableSchemaByName(tableName);
            if (t == null) {
                System.out.println("No such table: " + tableName + "\nERROR");
                return;
            }
    
            ArrayList<ArrayList<Object>> records = storageManager.getRecords(t.gettableNumber());
            if (records.isEmpty()) {
                System.out.println("No records found in table: " + tableName);
                return;
            }
    
            printRecords(records, t);
        } else {
            System.out.println("Expected table name after 'from'\nERROR");
        }
    }
    
    private static void printRecords(ArrayList<ArrayList<Object>> records, TableSchema table) {
        // Print header with attribute names
        System.out.print("|");
        for (AttributeSchema attr : table.getattributes()) {
            System.out.print(String.format(" %s |", attr.getname()));
        }
        System.out.println();
    
        // Print each record
        for (ArrayList<Object> record : records) {
            System.out.print("|");
            for (Object field : record) {
                System.out.print(String.format(" %s |", field.toString()));
            }
            System.out.println();
        }
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
                System.exit(0); // terminate the application
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
            if (tokens.length > 2 && tokens[1].equalsIgnoreCase("info")) {
                String tableName = tokens[2].replaceAll(";", "");
                if (!catalog.tableExists(tableName)) {
                    System.out.println("No such table " + tableName + "\nERROR");
                } else {
                    boolean found = catalog.findTableDisplay(tableName);
                    if (found) {
                        System.out.println("SUCCESS");
                    } else {
                        System.out.println("ERROR");
                    }
                }
            } else {
                displaySchema(catalog);
            }
            break;
        

            default:
                System.out.println("Unknown command: " + command);
        }
    }
}