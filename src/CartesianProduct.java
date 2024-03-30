import java.util.*;

public class CartesianProduct {
    public static Map<String, List<Object>> cartesianProduct(List<List<Record>> lists, List<String> columnsToSelect, List<TableSchema> tableSchemas) {
        List<List<Record>> result = new ArrayList<>();
        cartesianProductHelper(lists, result, new ArrayList<>(), 0);

        Map<String, List<Object>> tableValues = new HashMap<>();

        for (String column : columnsToSelect) {
            String tableName = column.substring(0, column.indexOf('.'));
            TableSchema currentTable = null;
            for (TableSchema tableSchema : tableSchemas) {
                if (tableSchema.getname().equals(tableName)) {
                    currentTable = tableSchema;
                    break;
                }
            }
            assert currentTable != null;
            tableValues.putIfAbsent(column, new ArrayList<>());
        }

        outerLoop:
        for (List<Record> recordList : result) {
            int recordsAdded = 0;
            int currentIndex = 0;
            for (Record record : recordList) {
                Object value;
                if (record.getNumElements() == 1) {
                    value = record.getData().get(0);
                    tableValues.get(columnsToSelect.get(currentIndex++)).add(value);
                    recordsAdded++;
                }
                else {
                    for (int j = 0; j < record.getNumElements(); j++) {
                        value = record.getData().get(j);
                        TableSchema currentTable = null;
                        for (TableSchema tableSchema : tableSchemas) {
                            if (tableSchema.getname().equals(columnsToSelect.get(currentIndex).substring(0, columnsToSelect.get(currentIndex).indexOf('.')))) {
                                currentTable = tableSchema;
                                break;
                            }
                        }
                        assert currentTable != null;
                        List<String> attributeNames = currentTable.getAttributeNames();
                        int index = 0;
                        for (int i = 0; i < attributeNames.size(); i++) {
                            if (attributeNames.get(i).equals(columnsToSelect.get(currentIndex).substring(columnsToSelect.get(currentIndex).indexOf('.') + 1))) {
                                index = i;
                                break;
                            }
                        }
                        if (index != j) {
                            continue;
                        }
                        tableValues.get(columnsToSelect.get(currentIndex++)).add(value);
                        recordsAdded++;
                        if (recordsAdded == columnsToSelect.size()) {
                            continue outerLoop;
                        }
                    }
                }
                if (recordsAdded == columnsToSelect.size()) {
                    continue outerLoop;
                }
            }
        }

        return tableValues;
    }

    private static void cartesianProductHelper(List<List<Record>> lists, List<List<Record>> result, List<Record> current, int index) {
        if (index == lists.size()) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (Record r : lists.get(index)) {
            current.add(r);
            cartesianProductHelper(lists, result, current, index + 1);
            current.remove(current.size() - 1);
        }
    }
}
