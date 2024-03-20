import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Condition;


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
    

    private static void handleDeleteCommand(String inputLine,StorageManager storageManager, Catalog catalog)
    {
        String[] parts = inputLine.split("\\s+");
        int indexOfSemicolon = inputLine.indexOf(';');

        if (indexOfSemicolon == -1) {  // If there are no semicolons...
            System.out.println("Expected ';'");
            return;
        } else if (!inputLine.endsWith(";")) {  // If the semicolon's position is not at the end of the statement...
            System.out.println("';' expected at the end of the statement");
            return;
        }
        
        if(!Arrays.asList(parts).contains("from")){
            System.out.println("Expected 'from'");
            return;
        }
        int index=2;
        String tablename=parts[index];
        boolean tableexists=catalog.tableExists(tablename);
        if(tableexists){
            int tableid=-1;
            for(TableSchema table: catalog.getTables()){
                if(table.getname().equals(tablename)){
                    tableid=table.gettableNumber();
                    break;
                }
            }
            index++;
            if(parts[index].equalsIgnoreCase("where")){
                System.out.println("Expected 'where'");
                return;
            }
            int startingindex=inputLine.indexOf("where");
            String whereclauseString=inputLine.substring(startingindex);
            ArrayList<ArrayList<Object>> records = storageManager.getRecords(tableid);
            List<List<WhereParse.Condition>> condition= WhereParse.parseWhereClause(whereclauseString);



        }
        else{
            System.out.println("Table does not exists.");
        }
    }

    private static void handleAlterCommand(String inputLine, Catalog catalog) {
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
        String attributestring="";

        if(tableexists==true){
            index++;
            if(sqlsplits[index].equalsIgnoreCase("add")){
                index++;
                attributestring=sqlsplits[index];
                for(int i=index+1; i < sqlsplits.length; i++){
                    attributestring+=" "+sqlsplits[i];
                }
                attributestring.trim();
                AttributeSchema attributeSchema=AttributeSchema.parse(attributestring);
                int tableid=0;
                for(int a = 0; a < catalog.getTableCount(); a++) {
                    
                    if(catalog.getTables().get(a).getname().equals(tablename)) {
                        tableid=a;
                        catalog.getTables().get(tableid).addAttribute(attributeSchema);
                        break;
                    }
                }
                
                // int n = catalog.getTables().get(tableid).getnumAttributes();
                // AttributeSchema[]newaAttributeSchemas=new AttributeSchema[catalog.getTables().get(tableid).getnumAttributes()+1];
                // for(int b = 0; b<n; b++) {  
                //     newaAttributeSchemas[b] = catalog.getTables().get(tableid).getattributes()[b];  
                // }
                // newaAttributeSchemas[n]=a;
                // catalog.getTables().get(tableid).setAttributes(newaAttributeSchemas);
                // catalog.getTables().get(tableid).setnumAttributes(n+1);
                


            }
            else if(sqlsplits[index].equalsIgnoreCase("drop") && Arrays.asList(sqlsplits).contains("column")){
                index+=2;
                String attributename=sqlsplits[index];
                int tableid=-1;
                for(int i = 0; i < catalog.getTableCount(); i++) {
                    
                    for(int j = 0; j < catalog.getTables().get(tableid).getnumAttributes(); j++){
                        AttributeSchema attributeSchema=catalog.getTables().get(tableid).getattributes()[j];
                        if(attributeSchema.getname().equals(attributename)){
                            tableid=i;
                            catalog.getTables().get(tableid).dropAttribute(attributename);
                            break;
                        }
                    }    
                }
                // if (attributenumber != -1 && tableid !=-1) {
                //     attributes=catalog.getTables().get(tableid).getattributes();
                //     for (int a = attributenumber; a < attributes.length - 1; a++) {
                //         attributes[a] = attributes[a + 1];
                //     }
                    
                //     // Resize the array
                //     AttributeSchema[] newArray = new AttributeSchema[attributes.length - 1];
                //     System.arraycopy(attributes, 0, newArray, 0, newArray.length);
                //     attributes = newArray;
                //     catalog.getTables().get(tableid).setAttributes(attributes);
                    
                // }
                
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
            String[] values = valueSet.split(",\\s*(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    
            if (values.length != table.getnumAttributes()) {
                System.out.println("Mismatch between number of columns and values provided.");
                return;
            }
    
            ArrayList<Object> recordValues = new ArrayList<>();
            for (int i = 0; i < values.length; i++) {
                String value = values[i].trim();
                AttributeSchema attribute = table.getattributes()[i];
                Object parsedValue = parseValueBasedOnType(value, attribute);
                if (parsedValue == null) {
                    System.out.println("Error parsing value: " + value + " for attribute: " + attribute.getname());
                    return;
                }
                recordValues.add(parsedValue);
            }
    
            int recordSize = calculateRecordSize(recordValues, table.getattributes()); // calc the record size 
            Record newRecord = new Record(recordValues, recordSize);
            storageManager.addRecord(catalog, newRecord, table.gettableNumber());
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
    private static int calculateRecordSize(ArrayList<Object> values, AttributeSchema[] attributes) {
        int size = 0;
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            AttributeSchema attr = attributes[i];
    
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