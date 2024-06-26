import java.util.*;

public class SelectParse {
    public static List<String> parseSelectClause(String columnNames) {
        List<String> columnList = new ArrayList<>();
        String[] columns = columnNames.split(",");

        for (String column : columns) {
            column = column.trim();  // Removes all whitespace
            columnList.add(column);
        }

        return columnList;
    }

    public static List<String> addTableNameToAttribute(List<String> columnList, List<TableSchema> tableSchemas) {
        List<String> columnsListWithTables = new ArrayList<>();
        for (String columnName : columnList) {
            String[] parts = columnName.split("\\.");

            String column = "";
            boolean found = false;

            if (parts.length == 2) {  // If a table name was provided
                String tableName = parts[0];
                column = parts[1];
                for (TableSchema table : tableSchemas) {
                    if (table.getName().equals(tableName)) {
                        List<String> attributeNames = table.getAttributeNames();
                        if(attributeNames.contains(column)) {
                            found = true;
                            columnsListWithTables.add(table.getName() + "." + column);
                            break;
                        }
                    }
                }
            }
            else {  // If no table name is provided
                column = columnName;
                List<TableSchema> foundInTables = new ArrayList<>();
                for (TableSchema table : tableSchemas) {
                    List<String> attributeNames = table.getAttributeNames();
                    if (attributeNames.contains(column)) {
                        foundInTables.add(table);
                        found = true;
                        columnsListWithTables.add(table.getName() + "." + column);
                    }
                }
                if (foundInTables.size() > 1) {
                    System.out.println("Column '" + column + "' is present in multiple tables: " + foundInTables);
                    return null;
                }
            }

            if (!found) {
                System.out.println("Column '" + column + "' does not exist in any of the provided tables.");
                return null;
            }
        }

        return columnsListWithTables;
    }

    public static List<String> reorderAttributes(List<String> columnsListWithTables, List<TableSchema> tableSchemas) {
        // Reorders attributes in the order that the tableSchemas were input in the from clause
        List<String> returnList = new ArrayList<>();
        for (TableSchema tableSchema : tableSchemas) {
            for (String column : columnsListWithTables) {
                if (column.substring(0, column.indexOf('.')).equals(tableSchema.getName())) {
                    returnList.add(column);
                }
            }
        }

        return returnList;
    }
}
