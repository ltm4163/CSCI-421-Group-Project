import java.util.LinkedList;
import java.util.List;

public class LeafNode extends Node {

    private LinkedList<Object> keys;
    private LinkedList<Pair<Integer, Integer>> pointers; //position of records in table file

    @Override
    public int search(int key) {
        return 0;
    }

    @Override
    public Object insert(Record record, int key, int pointer) {
        for (int i = 0; i < keys.size(); i++) {
            Object searchKey = keys.get(i);
            if (searchKey != null) {
                if (compare(key, searchKey) == 0) { //duplicate primarykey, cancel insert
                    System.err.println("Duplicate primarykey, insert cancelled");
                    return null;
                }
                if (compare(key, searchKey) > 0) {
                    keys.add(i+1, key);
                    double splitIndex = Math.ceil(order/2);
                    // TODO: implement splitting and propagate up tree
                    Pair<Integer, Integer> previousLocation = pointers.get(i);
                    StorageManager storageManager = Main.getStorageManager();
                    Page page = storageManager.getPage(tableNumber, previousLocation.getPageNumber());
                    page.shiftRecordsAndAdd(null, previousLocation.getIndex()); //TODO: add Record param to insert
                    if (page.isOverfull()) storageManager.splitPage(page);
                }
            }
        }
        return null; //placeholder
    }

    @Override
    public void delete(int key) {

    }
}
