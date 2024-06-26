import java.util.ArrayList;
import java.util.List;

public class FromParse {
    public static List<TableSchema> parseFromClause(String tableString, Catalog c) {
        List<String> tables = new ArrayList<>();
        List<TableSchema> tableSchemas = new ArrayList<>();
        tableString = tableString.replaceAll(";", "");  // Removes any possible semicolon
        String[] tableNames = tableString.split(",");

        for (String tableName : tableNames) {
            tables.add(tableName.trim());  // Removes whitespace
        }

        // Check if each table name is contained within the database
        for (String table : tables) {
            TableSchema t = c.getTableSchemaByName(table);
            if (t == null) {
                System.out.println("No such table: " + table + "\nERROR");
                return null;
            }
            tableSchemas.add(t);
        }

        return tableSchemas;
    }

    public static List<TableSchema> parseFromClause2(List<TableSchema> tableSchemas, List<String> columnsListWithTables) {
        // Reorders attributes in the order that the attributes were input in the select clause
        List<TableSchema> returnList = new ArrayList<>();
        for (String column : columnsListWithTables) {
            for (TableSchema tableSchema : tableSchemas) {
                if (column.substring(0, column.indexOf('.')).equals(tableSchema.getName())) {
                    if (!returnList.contains(tableSchema)) {
                        returnList.add(tableSchema);
                    }
                }
            }
        }
        return returnList;
    }
}
