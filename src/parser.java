import java.io.IOException;
import java.util.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        // Regular expressions to match SELECT, FROM, and WHERE clauses
        Pattern selectPattern = Pattern.compile("SELECT (.+?) FROM", Pattern.CASE_INSENSITIVE);
        Pattern fromPattern = Pattern.compile("FROM (.+?)(?: WHERE| ORDERBY|$)", Pattern.CASE_INSENSITIVE);
        Pattern wherePattern = Pattern.compile("WHERE (.+?)(?= ORDERBY|$)", Pattern.CASE_INSENSITIVE);
        Pattern orderByPattern = Pattern.compile("ORDERBY (.+)$", Pattern.CASE_INSENSITIVE);

        List<String> columnList;
        List<TableSchema> tableSchemas;
        WhereCondition whereRoot = null;

        // Match SELECT clause
        Matcher selectMatcher = selectPattern.matcher(inputLine);
        if (selectMatcher.find()) {
            String columnNames = selectMatcher.group(1);
            columnList = SelectParse.parseSelectClause(columnNames);
        } else {
            System.out.println("Error: No SELECT clause found");
            return;
        }

        // Match FROM clause
        Matcher fromMatcher = fromPattern.matcher(inputLine);
        if (fromMatcher.find()) {
            String tableNames = fromMatcher.group(1);
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
            columnList = SelectParse.addTableNameToAttribute(columnList, tableSchemas);
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

        // Orders the attributes in the order they were mentioned in the select clause
        tableSchemas = FromParse.parseFromClause2(tableSchemas, columnList);
        List<String> columnList2 = SelectParse.reorderAttributes(columnList, tableSchemas);

        // Match WHERE clause if present
        List<List<Record>> records = new ArrayList<>();
        Matcher whereMatcher = wherePattern.matcher(inputLine);
        if (whereMatcher.find()) {
            String whereClause = whereMatcher.group(1);
            whereClause = whereClause.trim().replaceAll(";$", "");
            whereRoot = WhereParse.parseWhereClause(whereClause);
            System.out.println("Debug: Parsed WHERE clause: " + whereRoot);

            final WhereCondition finalWhereRoot = whereRoot;

            if (whereRoot != null) {
                System.out.println("Debug: Where condition parse tree - " + whereRoot);
                for (TableSchema tableSchema : tableSchemas) {
                    List<Record> tableRecords = storageManager.getRecords(tableSchema.gettableNumber()).stream()
                            .map(rawData -> new Record(rawData, calculateRecordSize(rawData, tableSchema.getattributes()), new ArrayList<>()))
                            .filter(record -> {
                                System.out.println("Debug: Evaluating record: " + record);
                                return finalWhereRoot == null || finalWhereRoot.evaluate(record, tableSchema);
                            }).toList();
                    records.add(tableRecords);
                }
            }
        } else {
            //System.out.println("No WHERE conditions specified");
            for (TableSchema tableSchema : tableSchemas) {
                List<Record> tableRecords = storageManager.getRecords(tableSchema.gettableNumber()).stream()
                        .map(rawData -> new Record(rawData, calculateRecordSize(rawData, tableSchema.getattributes()), new ArrayList<>()))
                        .toList();
                records.add(tableRecords);
            }
        }

        if (records.get(0).isEmpty()) {
            return;
        }

        // Match ORDERBY clause if present
        boolean orderByCheck = false;
        Matcher orderByMatcher = orderByPattern.matcher(inputLine);
        if (orderByMatcher.find()) {
            orderByCheck = true;
            String normalizedOrderByColumn = normalizeColumnName(orderByMatcher.group(1).trim());
            if (normalizedOrderByColumn.indexOf('.') == -1) {
                boolean found = false;
                for (String column : columnList) {
                    if (column.substring(column.indexOf('.') + 1).equals(normalizedOrderByColumn)) {
                        normalizedOrderByColumn = column;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    System.err.println("Error: Column '" + normalizedOrderByColumn + "' does not exist in table schema.");
                    return;
                }
            }

            if (!normalizedOrderByColumn.isEmpty()) {
                List<List<Object>> rows = getValueMap(records, tableSchemas, columnList2, columnList);
                // TODO: Get the index of the attribute and sort by that index
                int index = 0;
                for (int i = 0; i < columnList.size(); i++) {
                    if (columnList.get(i).equals(normalizedOrderByColumn)) {
                        index = i;
                    }
                }
                final int attributeIndex = index;

                Collections.sort(rows, new Comparator<List<Object>>() {
                    @Override
                    public int compare(List<Object> o1, List<Object> o2) {
                        Object secondValue1 = o1.get(attributeIndex);
                        Object secondValue2 = o2.get(attributeIndex);
                        if (secondValue1 instanceof Integer) {
                            return ((Integer) secondValue1).compareTo((Integer) secondValue2);
                        } else if (secondValue1 instanceof Double) {
                            return ((Double) secondValue1).compareTo((Double) secondValue2);
                        } else if (secondValue1 instanceof String) {
                            return ((String) secondValue1).compareTo((String) secondValue2);
                        } else {
                            // Handle other types if needed
                            throw new IllegalArgumentException("Unsupported type");
                        }
                    }
                });
                printRowsByRow(rows, columnList);
            }
        }

        if (!orderByCheck) {
            printSelectedRecords(records, tableSchemas, columnList2, columnList);
        }
    }
    
    private static List<Record> fetchAndFilterRecords(WhereCondition whereRoot, TableSchema tableSchema, StorageManager storageManager) {
        return storageManager.getRecords(tableSchema.gettableNumber()).stream()
                .map(rawData -> new Record(rawData, calculateRecordSize(rawData, tableSchema.getattributes()), new ArrayList<>()))
                .filter(record -> whereRoot == null || whereRoot.evaluate(record, tableSchema))
                .collect(Collectors.toList());
    }   

    private static String normalizeColumnName(String columnName) {
        if (columnName == null) {
            return null;
        }
        int semiColonIndex = columnName.indexOf(";");
        String normalized = semiColonIndex != -1 ? columnName.substring(0, semiColonIndex) : columnName;
        return normalized.trim();
    }

    private static void printRowsByRow(List<List<Object>> rows, List<String> cartesianColumns) {
        for (String columnName : cartesianColumns) {
            System.out.print(columnName + " | ");
        }
        System.out.println();

        for (List<Object> objectList : rows) {
            StringBuilder row = new StringBuilder();
            for (Object object : objectList) {
                try {
                    row.append(object).append("\t\t");
                } catch (Exception e) {
                    row.append("null\t\t");  // Using null as a placeholder for this since there is no value
                }
            }
            System.out.println(row.toString().trim());
        }
    }

    private static void printSelectedRecords(List<List<Record>> records, List<TableSchema> tableSchemas, List<String> columnsToSelect, List<String> cartesianColumns) {
        Map<String, List<Object>> tableValues = new HashMap<>();
        int maxSize = 0;

        for (String columnName : cartesianColumns) {
            System.out.print(columnName + " | ");
            tableValues.putIfAbsent(columnName, new ArrayList<>());
        }
        System.out.println();

        // Record rows; TODO: need to make this it's own method to clean this up
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

        if (tableSchemas.size() == 1) {  // If it's a single table
            printRows(columnsToSelect, maxSize, tableValues);
        }

        else {  // If we need to do a Cartesian product...
            Map<String, List<Object>> cartesianMap = CartesianProduct.cartesianProduct(records, columnsToSelect, tableSchemas);
            maxSize = cartesianMap.get(cartesianColumns.get(0)).size();
            printRows(cartesianColumns, maxSize, cartesianMap);
        }
    }

    // TODO: Need to rename this method
    private static List<List<Object>> getValueMap(List<List<Record>> records, List<TableSchema> tableSchemas, List<String> columnsToSelect, List<String> cartesianColumns) {
        Map<String, List<Object>> tableValues = new HashMap<>();
        for (String columnName : cartesianColumns) {
            tableValues.putIfAbsent(columnName, new ArrayList<>());
        }

        int maxSize = 0;
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

        if (tableSchemas.size() == 1) {  // If it's a single table
            return getRows(columnsToSelect, maxSize, tableValues);
        }

        else {  // If we need to do a Cartesian product...
            Map<String, List<Object>> cartesianMap = CartesianProduct.cartesianProduct(records, columnsToSelect, tableSchemas);
            maxSize = cartesianMap.get(cartesianColumns.get(0)).size();
            return getRows(cartesianColumns, maxSize, cartesianMap);
        }
    }

    private static void printRows(List<String> columnsList, int maxSize, Map<String, List<Object>> map) {
        for (int i = 0; i < maxSize; i++) {
            StringBuilder row = new StringBuilder();
            for (String column : columnsList) {
                try {
                    row.append(map.get(column).get(i)).append("\t\t");
                } catch (Exception e) {
                    row.append("null\t\t");  // Using null as a placeholder for this since there is no value
                }
            }
            System.out.println(row.toString().trim());
        }
    }

    private static List<List<Object>> getRows(List<String> columnsList, int maxSize, Map<String, List<Object>> map) {
        List<List<Object>> returnValue = new ArrayList<>();
        for (int i = 0; i < maxSize; i++) {
            List<Object> values = new ArrayList<>();
            for (String column : columnsList) {
                try {
                    values.add(map.get(column).get(i));
                } catch (Exception ignored) {
                }
            }
            returnValue.add(values);
        }
        return returnValue;
    }

    private static void handleDeleteCommand(String inputLine, Catalog c, StorageManager storageManager) {
        // Regular expressions to match SELECT, FROM, and WHERE clauses
        Pattern fromPattern = Pattern.compile("FROM (.+?)(?: WHERE|$)", Pattern.CASE_INSENSITIVE);
        Pattern wherePattern = Pattern.compile("WHERE (.+)$", Pattern.CASE_INSENSITIVE);

        TableSchema tableSchema = null;
        List<List<WhereParse.Condition>> whereClauseList;
        WhereCondition whereRoot = null;

        // Match FROM clause
        Matcher fromMatcher = fromPattern.matcher(inputLine);
        if (fromMatcher.find()) {
            String tableNames = fromMatcher.group(1);
            System.out.println("Table names: " + tableNames);
            tableSchema = FromParse.parseFromClause(tableNames, c).get(0);
        } else {
            System.out.println("Error: No FROM clause found");
            return;
        }

        // Match WHERE clause if present
        Matcher whereMatcher = wherePattern.matcher(inputLine);
        if (whereMatcher.find()) {
            String whereClause = whereMatcher.group(1);
            System.out.println("Where conditions: " + whereClause);
            whereRoot = WhereParse.parseWhereClause(whereClause); //TODO: change this to WhereParse version after merge
            // whereClauseList = WhereParse.parseWhereClause(whereClause);
        } else {
            System.out.println("No WHERE conditions specified");
        }

        storageManager.deleteRecords(tableSchema, whereRoot);
    }

    public static void handleUpdateCommand(String inputLine, Catalog catalog, StorageManager storageManager) {
        Pattern pattern = Pattern.compile("^update\\s+(\\w+)\\s+set\\s+(\\w+)\\s*=\\s*(.+?)\\s+where\\s+(.+);$");

        // Create a Matcher object to apply the pattern to the input update statement
        Matcher matcher = pattern.matcher(inputLine);

        String tableName;
        String columnName;
        String value;
        String condition;

        // Check if the update statement matches the pattern
        if (matcher.matches()) {
            tableName = matcher.group(1);
            columnName = matcher.group(2);
            value = matcher.group(3);
            condition = matcher.group(4);

            System.out.println("DEBUG | tableName: " + tableName);
            System.out.println("DEBUG | columnName: " + columnName);
            System.out.println("DEBUG | value : " + value);
            System.out.println("DEBUG | condition " + condition);
        } else {
            System.out.println("Invalid update statement format\nERROR");
            return;
        }

        if(condition != null) {
            condition = condition.trim().replaceAll(";$", "");
        } else { // can the update work with no condition?
            System.out.println("No condition specified\nERROR");
        }

        TableSchema tableSchema = catalog.getTableSchemaByName(tableName);
        if(tableSchema == null) {
            System.out.println("Table '" + tableName + "' does not exist\nERROR");
            return;
        }

        if(!tableSchema.hasAttribute(columnName)) {
            System.out.println("Column '" + columnName + "' does not exist in table '" + tableName + "'\nERROR");
        }

        WhereCondition whereRoot = null;
        if(condition != null && !condition.isBlank()) {
            whereRoot = WhereParse.parseWhereClause(condition);
            System.out.println("DEBUG | parse WHERE clause: " + whereRoot);
        }

        final WhereCondition finalWhereRoot = whereRoot;

        Object objectValue = value;
        if (whereRoot != null) {
            // Update records based on the condition
            boolean success = storageManager.updateRecord(tableName, columnName, objectValue, whereRoot);
            if (success) {
                System.out.println("Update successful");
            } else {
                System.out.println("Update failed");
            }
        } else {
            // Update all records (no condition specified)
            boolean success = storageManager.updateRecord(tableName, columnName, objectValue, whereRoot);
            if (success) {
                System.out.println("Update successful");
            } else {
                System.out.println("Update failed");
            }
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

            case "delete":
                handleDeleteCommand(inputLine, catalog, storageManager);
                break;

            case "update":
                 handleUpdateCommand(inputLine, catalog, storageManager);
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