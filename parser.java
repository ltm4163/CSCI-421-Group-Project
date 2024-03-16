import java.io.IOException;

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
        // TODO: Implement the handleAlterCommand method
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
        String [] sqlsplits = sqlcommand.split(" ");

        if(!Arrays.asList(sqlsplits).contains("table")){
            System.out.println("Expected 'table'");
            return;
        }
        int index =2;
        String tablename = sqlsplits[2];
        boolean tableexists=catalog.tableExists(tablename);
        if(tableexists==true){
            index++;
            if(sqlsplits[index].equalsIgnoreCase("add")){
                index++;
                AttributeSchema a=new AttributeSchema(tablename, tablename, false, false, false, 1);
                String attributename=sqlsplits[index];
                a.setname(attributename);
                index++;
                String attributetype=sqlsplits[index];
                a.settype(attributetype);

                boolean notnull = Arrays.asList(sqlsplits).contains("not") && Arrays.asList(sqlsplits).contains("null");
                boolean primarykey=Arrays.asList(sqlsplits).contains("primary") && Arrays.asList(sqlsplits).contains("key");
                boolean unique = Arrays.asList(sqlsplits).contains("unique");
                a.setnotnull(notnull);
                a.setprimarykey(primarykey);
                a.setunique(unique);
                int tableid=0;
                for(int i = 0; i < catalog.getTableCount(); i++) {
                    
                    if(catalog.getTables().get(i).getname().equals(tablename)) {
                        tableid=i;
                        break;
                    }
                }
                
                int n = catalog.getTables().get(tableid).getnumAttributes();
                AttributeSchema[]newaAttributeSchemas=new AttributeSchema[catalog.getTables().get(tableid).getnumAttributes()+1];
                for(int b = 0; b<n; b++) {  
                    newaAttributeSchemas[b] = catalog.getTables().get(tableid).getattributes()[b];  
                }
                newaAttributeSchemas[n]=a;
                catalog.getTables().get(tableid).setAttributes(newaAttributeSchemas);
                catalog.getTables().get(tableid).setnumAttributes(n+1);
                


            }
            else if(sqlsplits[index].equalsIgnoreCase("drop") && Arrays.asList(sqlsplits).contains("column")){
                index+=2;
                String attributename=sqlsplits[index];
                int attributenumber=-1;
                int tableid=-1;
                AttributeSchema[] attributes;
                for(int i = 0; i < catalog.getTableCount(); i++) {
                    TableSchema tableSchema=catalog.getTables().get(i);
                    for(int j = 0; j < tableSchema.getnumAttributes(); j++){
                        AttributeSchema attributeSchema=tableSchema.getattributes()[j];
                        if(attributeSchema.getname().equals(attributename)){
                            attributenumber=j;
                            tableid=i;
                            break;
                        }
                    }    
                }
                if (attributenumber != -1 && tableid !=-1) {
                    attributes=catalog.getTables().get(tableid).getattributes();
                    for (int a = attributenumber; a < attributes.length - 1; a++) {
                        attributes[a] = attributes[a + 1];
                    }
                    
                    // Resize the array
                    AttributeSchema[] newArray = new AttributeSchema[attributes.length - 1];
                    System.arraycopy(attributes, 0, newArray, 0, newArray.length);
                    attributes = newArray;
                    catalog.getTables().get(tableid).setAttributes(attributes);
                    
                }
                
            }
            else if(sqlsplits[index].equalsIgnoreCase("rename") &&Arrays.asList(sqlsplits).contains("column")){
                index+=2;
                String oldattributename=sqlsplits[index];
                index++;
                if(sqlsplits[index].equalsIgnoreCase("to")){
                    System.out.println("Expected 'to'");
                    return;
                }
                index++;
                String newattributename=sqlsplits[index];
                int attributenumber=-1;
                int tableid=-1;
                for(int i = 0; i < catalog.getTableCount(); i++) {
                    TableSchema tableSchema=catalog.getTables().get(tableid);
                    for(int j = 0; j < tableSchema.getnumAttributes(); j++){
                        AttributeSchema attributeSchema=tableSchema.getattributes()[j];
                        if(attributeSchema.getname().equals(oldattributename)){
                            attributenumber=j;
                            tableid=i;
                            break;
                        }
                    }    
                }
                catalog.getTables().get(tableid).getattributes()[attributenumber].setname(newattributename);
            }
            else if(sqlsplits[index].equalsIgnoreCase("alter") &&Arrays.asList(sqlsplits).contains("column")){
                index+=2;
                String attributename=sqlsplits[index];
                index++;
                String attributetype=sqlsplits[index];
                int attributenumber=-1;
                int tableid=-1;
                for(int i = 0; i < catalog.getTableCount(); i++) {
                    TableSchema tableSchema=catalog.getTables().get(tableid);
                    for(int j = 0; j < tableSchema.getnumAttributes(); j++){
                        AttributeSchema attributeSchema=tableSchema.getattributes()[j];
                        if(attributeSchema.getname().equals(attributename)){
                            attributenumber=j;
                            tableid=i;
                            break;
                        }
                    }    
                }
                catalog.getTables().get(tableid).getattributes()[attributenumber].settype(attributetype);
            }


        }
        else{
            System.out.println("Table does not exists.");
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

    private static void intToBytes(int i, Object getdata, int ptrIndex) {
        // TODO: Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'intToBytes'");
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
                displaySchema(catalog);
                break;

            default:
                System.out.println("Unknown command: " + command);
        }
    }
}