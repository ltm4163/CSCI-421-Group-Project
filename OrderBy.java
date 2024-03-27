import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderBy {
     public static void parseorderby(String inputlineString, Catalog c, StorageManager storageManager) {
        String[] parts = inputlineString.split("\\s+");
        int indexOfSemicolon = inputlineString.indexOf(';');

        if (indexOfSemicolon == -1) {  // If there are no semicolons...
            System.out.println("Expected ';'");
            return;
        } else if (!inputlineString.endsWith(";")) {  // If the semicolon's position is not at the end of the statement...
            System.out.println("';' expected at the end of the statement");
            return;
        }
        
        if(!Arrays.asList(parts).contains("from")){
            System.out.println("Expected 'from'");
            return;
        }
        Pattern patterncolumns = Pattern.compile("SELECT\\s+(.*?)\\s+FROM", Pattern.CASE_INSENSITIVE);
        Matcher matchercolumns = patterncolumns.matcher(inputlineString);
        List<String> columnNames = new ArrayList<>();
        if (matchercolumns.find()) {
            String columns = matchercolumns.group(1);
            
            String[] columnstrings = columns.split(",");
            for (String columnstring : columnstrings) {
            // Trim whitespace and add to list
                columnNames.add(columnstring.trim());
            }
        } else {
            System.out.println("Column names not found!");
            return;
        }
        String tableName="";
        Pattern patterntable = Pattern.compile("FROM\\s+([\\w]+)\\b", Pattern.CASE_INSENSITIVE);
        Matcher matchertable = patterntable.matcher(inputlineString);
        if (matchertable.find()) {
            tableName = matchertable.group(1);
        } else {
            System.out.println("Table name not found!");
            return;
        }
        Pattern patternorder = Pattern.compile("ORDER\\s+BY\\s+(.+?)(?:;|$)", Pattern.CASE_INSENSITIVE);
        Matcher matcheroder = patternorder.matcher(inputlineString);
        String orderByClause="";
        if(matcheroder.find()) {
            orderByClause = matcheroder.group(1);
        } else {
            System.out.println("ORDER BY clause not found!");
        }
        boolean tableexist=c.tableExists(tableName);
        int tableid=0;
        if(tableexist){
            
            for(TableSchema table: c.getTables()){
                if(table.getname().equals(tableName)){
                    tableid=table.gettableNumber();
                    break;
                }
            }
        }
        else{
            System.out.println("Table not found");
            return;
        }
        
        ArrayList<ArrayList<Object>>records=storageManager.getRecords(tableid);
        TableSchema tableSchema=c.getTables().get(tableid);
        AttributeSchema [] tableatAttributeSchemas=tableSchema.getattributes();
        HashMap<String, Integer> attributemap = new HashMap<>();
        int valuenumber=0;
        for(AttributeSchema attributeSchema : tableatAttributeSchemas){
            valuenumber++;
            if(columnNames.contains(attributeSchema.getname())){
                attributemap.put(attributeSchema.getname(), valuenumber);
            }
        }
        /**
        ArrayList<ArrayList<Object>>newrecords=new ArrayList<>();
        for(ArrayList<Object>record: records){
            ArrayList<Object>newrecord=new ArrayList<>();
            for(Map.Entry<String, Integer> entry : attributemap.entrySet()){
                newrecord.add(record.get(entry.getValue()));
            }
            newrecords.add(newrecord);
        }
        */
        ArrayList<ArrayList<Object>>displayrecords=new ArrayList<>();
        String [] orderbyparts=orderByClause.split(" ");
        if(orderbyparts.length ==1){
            String orderattribute=orderbyparts[0];
            int recordindex=attributemap.get(orderattribute);
            ArrayList<Object> firstRecord= records.get(0);
            Object object=firstRecord.get(recordindex);
            if(object instanceof Integer){
                for(int i=0; i < records.size(); i++){
                    int currentint=(Integer)records.get(i).get(recordindex);
                    for(int j=0; j < records.size(); j++){
                        if(currentint > (Integer)records.get(j).get(recordindex)){
                            ArrayList<Object>tempArrayList=records.get(j);
                            records.set(j, records.get(i));
                            records.set(i, tempArrayList);
                        }
                    }
                }
            }
            if(object instanceof Double){
                for(int i=0; i < records.size(); i++){
                    double currentdouble=(Double)records.get(i).get(recordindex);
                    for(int j=0; j < records.size(); j++){
                        if(currentdouble > (Double)records.get(j).get(recordindex)){
                            ArrayList<Object>tempArrayList=records.get(j);
                            records.set(j, records.get(i));
                            records.set(i, tempArrayList);
                        }
                    }
                }

            }
            if(object instanceof Float){
                for(int i=0; i < records.size(); i++){
                    float currentfloat=(Float)records.get(i).get(recordindex);
                    for(int j=0; j < records.size(); j++){
                        if(currentfloat > (Float)records.get(j).get(recordindex)){
                            ArrayList<Object>tempArrayList=records.get(j);
                            records.set(j, records.get(i));
                            records.set(i, tempArrayList);
                        }
                    }
                }
            }
            if(object instanceof String){
                for(int i=0; i < records.size(); i++){
                    String currentstring=(String)records.get(i).get(recordindex);
                    for(int j=0; j < records.size(); j++){
                        int result=currentstring.compareTo((String)records.get(j).get(recordindex));
                        if(result > 0){
                            ArrayList<Object>tempArrayList=records.get(j);
                            records.set(j, records.get(i));
                            records.set(i, tempArrayList);
                        }
                    }
                }
            }
        }
        for(ArrayList<Object>record: records){
            ArrayList<Object>newrecord=new ArrayList<>();
            for(Map.Entry<String, Integer> entry : attributemap.entrySet()){
                newrecord.add(record.get(entry.getValue()));
            }
            displayrecords.add(newrecord);
        }
     }
}
