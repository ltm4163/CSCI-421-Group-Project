import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
                // Adds new attribute's default value to each existing record
                for (Record record : storageManager.getPhysicalRecords(table.gettableNumber())) {
                    ArrayList<Object> data = record.getData();
                    AttributeSchema[] attributes = table.getattributes();
                    int newAttributeIndex = attributes.length - 1;
                    if (definition.contains("default")) {
                        String[] definitionParts = definition.split("\\s+");
                        String defaultValue = definitionParts[definitionParts.length - 1];
                        attributes[newAttributeIndex].setDefaultValue(defaultValue);
                    }
                    data.add(attributes[newAttributeIndex].getDefaultValue());
                    record.setData(data);
                    record.setBitMapValue(table.getnumAttributes()-1);
                }
                System.out.println("Attribute " + newAttr.getname() + " added to table " + tableName + ".");
                break;
            case "drop":
                AttributeSchema[] attributes = table.getattributes();
                IntStream.range(0, attributes.length).forEach(i -> {
                    AttributeSchema attribute = attributes[i];
                    System.out.println(attribute.getname());
                    System.out.println(definition);
                    System.out.println(i);
                    if (attribute.getname().equals(definition)) {
                        for (Record record : storageManager.getPhysicalRecords(table.gettableNumber())) {
                            ArrayList<Object> data = record.getData();
                            data.remove(i);
                            System.out.println(data);
                            record.setData(data);
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
        // Regular expressions to match SELECT, FROM, and WHERE clauses
        Pattern selectPattern = Pattern.compile("SELECT (.+?) FROM", Pattern.CASE_INSENSITIVE);
        Pattern fromPattern = Pattern.compile("FROM (.+?)(?: WHERE|$)", Pattern.CASE_INSENSITIVE);
        Pattern wherePattern = Pattern.compile("WHERE (.+)$", Pattern.CASE_INSENSITIVE);

        List<String> columnList;
        List<TableSchema> tableSchemas;
        WhereCondition whereRoot = null;

        // Match SELECT clause
        Matcher selectMatcher = selectPattern.matcher(inputLine);
        if (selectMatcher.find()) {
            String columnNames = selectMatcher.group(1);
            System.out.println("Columns to select: " + columnNames);
            columnList = SelectParse.parseSelectClause(columnNames);
        } else {
            System.out.println("Error: No SELECT clause found");
            return;
        }

        // Match FROM clause
        Matcher fromMatcher = fromPattern.matcher(inputLine);
        if (fromMatcher.find()) {
            String tableNames = fromMatcher.group(1);
            System.out.println("Table names: " + tableNames);
            tableSchemas = FromParse.parseFromClause(tableNames, c);
            if (tableSchemas == null) {
                return;
            }
        } else {
            System.out.println("Error: No FROM clause found");
            return;
        }

        if (!(columnList.size() == 1 && columnList.get(0).equals("*"))) {
            // Ensures column names exist in tables
            columnList = SelectParse.parseSelectClause2(columnList, tableSchemas, c);
            if (columnList == null) {
                return;
            }
        }
        else {  // If the select parameter is '*'
            columnList.clear();
            for (TableSchema tableSchema : tableSchemas) {
                columnList.addAll(tableSchema.getAttributeNamesWithTable());
            }
        }

        // Match WHERE clause if present
        Matcher whereMatcher = wherePattern.matcher(inputLine);
        if (whereMatcher.find()) {
            String whereClause = whereMatcher.group(1);
            System.out.println("Where conditions: " + whereClause);
            whereClause = whereClause.trim().replaceAll(";$", "");
            whereRoot = WhereParse.parseWhereClause(whereClause);
            System.out.println("Debug: Parsed WHERE clause: " + whereRoot);

            final WhereCondition finalWhereRoot = whereRoot;

            if (whereRoot != null) {
                System.out.println("Debug: Where condition parse tree - " + whereRoot.toString());

                List<List<Record>> records = new ArrayList<>();
                for (TableSchema tableSchema : tableSchemas) {
                    List<Record> tableRecords = storageManager.getRecords(tableSchema.gettableNumber()).stream()
                            .map(rawData -> new Record(rawData, calculateRecordSize(rawData, tableSchema.getattributes()), new ArrayList<>()))
                            .filter(record -> {
                                System.out.println("Debug: Evaluating record: " + record);
                                return finalWhereRoot == null || finalWhereRoot.evaluate(record, tableSchema);
                            }).toList();
                    records.add(tableRecords);
                }
                printSelectedRecords(records, tableSchemas, columnList);
            }
        } else {
            System.out.println("No WHERE conditions specified");
            List<List<Record>> records = new ArrayList<>();
            for (TableSchema tableSchema : tableSchemas) {
                List<Record> tableRecords = storageManager.getRecords(tableSchema.gettableNumber()).stream()
                        .map(rawData -> new Record(rawData, calculateRecordSize(rawData, tableSchema.getattributes()), new ArrayList<>()))
                        .toList();
                records.add(tableRecords);
            }
            printSelectedRecords(records, tableSchemas, columnList);
        }
    }

    // Take in Map<TableSchema, List<Record>> that has all records already mapped to their specific table schema?
    private static void printSelectedRecords(List<List<Record>> records, List<TableSchema> tableSchemas, List<String> columnsToSelect) {
        Map<String, List<Object>> tableValues = new HashMap<>();
        int maxSize = 0;

        for (String columnName : columnsToSelect) {
            System.out.print(columnName + " | ");
            tableValues.putIfAbsent(columnName, new ArrayList<>());
        }
        System.out.println();

        // Record rows
        int columnIndex = 0;
        outerLoop:
        for (List<Record> recordList : records) {
            if (recordList.get(0).getNumElements() == 1) {  // select case with one attribute
                List<Object> values = new ArrayList<>();
                for (Record record : recordList) {
                    values.add(record.getData().get(0));
                }
                tableValues.get(columnsToSelect.get(columnIndex++)).addAll(values);
                if (columnsToSelect.size() == columnIndex) {
                    if (recordList.size() > maxSize) {
                        maxSize = recordList.size();
                    }
                    break;
                }
            }
            else {  // select case with multiple attributes
                for (int j = 0; j < recordList.get(0).getNumElements(); j++) {
                    List<Object> values = new ArrayList<>();
                    String currentColumn = columnsToSelect.get(columnIndex);
                    TableSchema currentTable = null;
                    for (TableSchema tableSchema : tableSchemas) {
                        if (tableSchema.getName().equals(currentColumn.substring(0, currentColumn.indexOf('.')))) {
                            currentTable = tableSchema;
                        }
                    }
                    for (Record record : recordList) {
                        assert currentTable != null;
                        values.add(record.getAttributeValue(currentColumn.substring(currentColumn.indexOf('.') + 1), currentTable.getattributes()));
                    }
                    tableValues.get(columnsToSelect.get(columnIndex++)).addAll(values);
                    if (columnsToSelect.size() == columnIndex) {
                        if (recordList.size() > maxSize) {
                            maxSize = recordList.size();
                        }
                        break outerLoop;
                    }
                    else if (!columnsToSelect.get(columnIndex).substring(0, currentColumn.indexOf('.')).equals(Objects.requireNonNull(currentTable).getname())) {
                        if (recordList.size() > maxSize) {
                            maxSize = recordList.size();
                        }
                        break;
                    }
                }
            }

            if (recordList.size() > maxSize) {
                maxSize = recordList.size();
            }
        }

        for (int i = 0; i < maxSize; i++) {
            StringBuilder row = new StringBuilder();
            for (String column : columnsToSelect) {
                try {
                    row.append(tableValues.get(column).get(i)).append("\t\t");
                }
                catch (Exception e) {
                    row.append("null\t\t");  // Using null as a placeholder for this since there is no value
                }
            }
            System.out.println(row.toString().trim());
        }
    }

    // Unnecessary method
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