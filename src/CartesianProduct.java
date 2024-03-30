import java.util.*;

public class CartesianProduct {
    public static Map<String, List<Object>> cartesianProduct(List<List<Record>> lists, List<String> columnsToSelect, List<TableSchema> tableSchemas) {
        List<String> tablesInOrder = new ArrayList<>();
        for (String column : columnsToSelect) {
            if (!tablesInOrder.contains(column.substring(0, column.indexOf('.')))) {
                tablesInOrder.add(column.substring(0, column.indexOf('.')));
            }
        }

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
            //System.out.println(recordList);
            int recordsAdded = 0;
            int currentIndex = 0;
            thisLoop:
            for (int i = 0; i < recordList.size(); i++) {
                Object value;
                if (recordList.get(i).getNumElements() == 1) {
                    value = recordList.get(i).getData().get(0);
                    tableValues.get(columnsToSelect.get(currentIndex++)).add(value);
                    recordsAdded++;
                }
                else {
                    for (int j = 0; j < recordList.get(i).getNumElements(); j++) {
                        //System.out.println(recordList.get(i));
                        value = recordList.get(i).getData().get(j);
                        TableSchema currentTable = null;
                        for (TableSchema tableSchema : tableSchemas) {
                            if (tableSchema.getname().equals(tablesInOrder.get(i))) {
                                currentTable = tableSchema;
                                if (!currentTable.getname().equals(columnsToSelect.get(currentIndex).substring(0, columnsToSelect.get(currentIndex).indexOf('.')))) {
                                    continue thisLoop;
                                }
                                break;
                            }
//                            if (tableSchema.getname().equals(columnsToSelect.get(currentIndex).substring(0, columnsToSelect.get(currentIndex).indexOf('.')))) {
//                                currentTable = tableSchema;
//                                break;
//                            }
                        }
                        assert currentTable != null;
                        List<String> attributeNames = currentTable.getAttributeNamesWithTable();
                        int index = 0;
                        for (int x = 0; x < attributeNames.size(); x++) {
                            if (attributeNames.get(x).equals(columnsToSelect.get(currentIndex))) {
                                index = x;
                                break;
                            }
                        }
//                        System.out.println(index + " " + j);
//                        System.out.println(tablesInOrder.get(j));
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
