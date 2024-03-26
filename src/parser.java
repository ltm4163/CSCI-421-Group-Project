import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

        List<AttributeSchema> attributesWithPrimaryKey = attributes.stream().filter(attr -> attr.getprimarykey() == true)
                                                            .collect(Collectors.toList());
        if (attributesWithPrimaryKey.size()!=1) {
            System.err.println("Tables should have 1 primary key");
            System.err.println("Error");
            return;
        }

        TableSchema foundTable = catalog.getTables().stream()
                                 .filter(table -> table.getName().equals(tableName))
                                 .findFirst()
                                 .orElse(null);
        if (foundTable != null) {
            System.err.println("Table of name " + tableName + " already exists");
            System.err.println("ERROR");
            return;
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
    
    private static void handleAlterCommand(String inputLine, Catalog catalog, StorageManager storageManager) {
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
                AttributeSchema newAttr = AttributeSchema.parse(definition);
                table.addAttribute(newAttr);
                AttributeSchema[] attributes = table.getattributes();
                int newAttributeIndex = attributes.length - 1;
                // Adds new attribute's default value to each existing record
                List<Page> pages = storageManager.getPages(table.gettableNumber());
                for (Page page : pages) {
                    for (Record record : page.getRecords()) {
                        if (definition.contains("default")) {
                            String[] definitionParts = definition.split("\\s+");
                            String defaultValue = definitionParts[definitionParts.length - 1];
                            attributes[newAttributeIndex].setDefaultValue(defaultValue);
                        }
                        int sizeAdded = record.addValue(attributes[newAttributeIndex].getDefaultValue(), newAttributeIndex, attributes[newAttributeIndex]);
                        page.setSize(page.getSize() + sizeAdded);
                    }
                    if (page.getSize() > Main.getPageSize()) storageManager.splitPage(page);
                }
                System.out.println("Attribute " + newAttr.getname() + " added to table " + tableName + ".");
                break;
            case "drop":
                AttributeSchema[] attributes2 = table.getattributes();
                int newAttributeIndex2 = attributes2.length - 1;
                List<Page> pages2 = storageManager.getPages(table.gettableNumber());
                IntStream.range(0, attributes2.length).forEach(i -> {
                    AttributeSchema attribute = attributes2[i];
                    System.out.println(attribute.getname());
                    System.out.println(definition);
                    System.out.println(i);
                    if (attribute.getname().equals(definition)) {
                        for (Page page : pages2) {
                            for (Record record : page.getRecords()) {
                                int sizeLost = record.removeValue(newAttributeIndex2, attributes2[newAttributeIndex2]);
                                page.setSize(page.getSize() - sizeLost);
                            }
                        }
                    }
                });

                // Assuming table has a method to drop an attribute
                table.dropAttribute(definition);
                System.out.println("Attribute " + definition + " dropped from table " + tableName + ".");
                break;
            default:
                System.out.println("Unsupported ALTER TABLE operation: " + operation);
        }
    }

    
    private static void handleInsertCommand(String inputLine, Catalog catalog, StorageManager storageManager) {
        String[] parts = inputLine.trim().split("\\s+", 4);
    
        if (parts.length < 4 || !parts[0].equalsIgnoreCase("insert") || !parts[1].equalsIgnoreCase("into")) {
            System.out.println("Syntax error in INSERT INTO command.");
            return;
        }
    
        String tableName = parts[2];
        TableSchema table = catalog.getTableSchemaByName(tableName);
        if (table == null) {
            System.out.println("Table not found: " + tableName);
            return;
        }
    
        String valuesPart = parts[3].substring(parts[3].indexOf("("));
        if (!valuesPart.endsWith(";")) {
            System.out.println("Expected ';' at the end of the command.");
            return;
        }
    
        valuesPart = valuesPart.substring(0, valuesPart.length() - 1); 
    
        String[] individualValueSets = valuesPart.split("\\),\\s*\\(");
        for (String valueSet : individualValueSets) {
            valueSet = valueSet.trim().replaceAll("^\\(|\\)$", "");
            String[] values = valueSet.split("\\s+(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    
            if (values.length != table.getnumAttributes()) {
                System.out.println("Mismatch between number of columns and values provided.");
                return;
            }
    
            ArrayList<Byte> nullBitMap = new ArrayList<>(table.getnumAttributes());
            ArrayList<Object> recordValues = new ArrayList<>();
            for (int i = 0; i < values.length; i++) {
                String value = values[i].trim();
                AttributeSchema attribute = table.getattributes()[i];

                // if null value
                if (value instanceof String && ((String) value).equalsIgnoreCase("null")) {
                    recordValues.add(null);
                    nullBitMap.add((byte)1);
                    continue;
                }

                Object parsedValue = parseValueBasedOnType(value, attribute);
                if (parsedValue == null) {
                    System.err.println("Error parsing value: " + value + " for attribute: " + attribute.getname());
                    return;
                }
                else if (parsedValue instanceof String) {  // If the parsed value is a char or varchar
                    // char
                    if (attribute.gettype().equals("char") &&
                            ((String) parsedValue).length() > attribute.getsize()) {
                        System.err.println("Expected char length of: " + attribute.getsize() +
                                " for attribute: " + attribute.getname());
                        return;
                    }
                    // varchar
                    else if (attribute.gettype().equals("varchar") &&
                            ((String) parsedValue).length() > attribute.getsize()){
                        System.err.println("Expected varchar length of less than or equal to: " + attribute.getsize() +
                                " for attribute: " + attribute.getname());
                        return;
                    }
                }
                recordValues.add(parsedValue);
                nullBitMap.add((byte)0);
            }
    
            int recordSize = calculateRecordSize(recordValues, table.getattributes()); // calc the record size
            Record newRecord = new Record(recordValues, recordSize, nullBitMap);
            boolean worked = storageManager.addRecord(catalog, newRecord, table.gettableNumber());
            if (!worked) {
                return;
            }
        }
    
        System.out.println("Record(s) inserted successfully into table: " + tableName);
    }
    
    private static Object parseValueBasedOnType(String value, AttributeSchema attribute) {
        try {
            switch (attribute.gettype().toLowerCase()) {
                case "integer":
                    return Integer.parseInt(value);
                case "double":
                    return Double.parseDouble(value);
                case "boolean":
                    return Boolean.parseBoolean(value);
                case "char":
                case "varchar":
                    if(value.startsWith("'") && value.endsWith("'")) {
                        return value.substring(1, value.length() - 1);
                    }
                    // Handle error or assume it's a correct string
                    return value;
                default:
                    System.out.println("Unsupported attribute type: " + attribute.gettype());
                    return null;
            }
        } catch (NumberFormatException e) {
            System.out.println("Error parsing value: " + value);
            return null;
        }
    }
    
    
    private static int calculateRecordSize(ArrayList<Object> values, AttributeSchema[] attributes) {
        int size = 0;
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            AttributeSchema attr = attributes[i];
    
            if (value == null) continue;
            switch (attr.gettype().toLowerCase()) {
                case "integer":
                    size += Integer.BYTES;
                    break;
                case "double":
                    size += Double.BYTES;
                    break;
                case "boolean":
                    size += 1;
                    break;
                case "char":
                    size += attr.getsize();
                    break;
                case "varchar":
                    String stringValue = (String) value;
                    size += stringValue.getBytes().length;
                    // Include 4 bytes to store the length of varchar if needed
                    size += Integer.BYTES;
                    break;
                default:
                    System.out.println("Unsupported attribute type: " + attr.gettype());
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
                if (field != null) {
                    System.out.print(String.format(" %s |", field.toString()));
                }
                else {
                    System.out.print(" null |");
                }
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
                handleAlterCommand(inputLine, catalog, storageManager);
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