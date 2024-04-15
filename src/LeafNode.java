import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class LeafNode extends Node {

    private LinkedList<Object> keys;
    private LinkedList<Pair<Integer, Integer>> pointers; //position of records in table file

    public LeafNode(int order, String attrType, int tableNumber, boolean isRoot) {
        this.order = order;
        this.tableNumber = tableNumber;
        this.attrType = attrType;
        this.isRoot = isRoot;
        this.isLeaf = true;
        this.keys = new LinkedList<>();
        this.pointers = new LinkedList<>();
    }

    @Override
    public int search(int key) {
        return 0;
    }

    @Override
    public Object insert(Record record, int key, int pointer) {
        if (keys.size() == 0) { //create key & pointer lists, add key
            keys.add(key);
            System.out.println("adding: " + key);
            pointers.add(new Pair<Integer,Integer>(pointer, pointer)); //TODO: change this and param to actual pointer
            return null;
        }

        boolean inserted = false;
        for (int i = 0; i < keys.size(); i++) { //find where to insert key in node
            Object searchKey = keys.get(i);
            if (searchKey != null) {
                if (compare(key, searchKey) == 0) { //duplicate primarykey, cancel insert
                    System.err.println("Duplicate primarykey, insert cancelled");
                    return null;
                }
                if (compare(key, searchKey) < 0) { //insert key at this position
                    keys.add(i, key);
                    System.out.println("adding: " + key);
                    inserted = true;
                    return null;
                }
            }
        }
        if (inserted == false) { //insert key at the end of the node
            keys.add(key);
            System.out.println("adding: " + key);
        }

        if (keys.size() == order) { // if values in leaf = N, split node
            double splitIndex = Math.ceil(order/2); // split point for leaf nodes
            LeafNode newNode = new LeafNode(order, attrType, tableNumber, false);
            List<Object> clonedKeys = keys.subList((int)splitIndex, keys.size()).stream().map(object -> object)
                        .collect(Collectors.toList());
            keys.subList((int)splitIndex, keys.size()).clear();
            System.out.println("og list");
            for (Object object2 : keys) System.out.println(object2);
            System.out.println("new list");
            for (Object object3 : clonedKeys) System.out.println(object3);
            // TODO: implement splitting and propagate up tree
        }
        // Pair<Integer, Integer> previousLocation = pointers.get(i);
        // StorageManager storageManager = Main.getStorageManager();
        // Page page = storageManager.getPage(tableNumber, previousLocation.getPageNumber());
        // page.shiftRecordsAndAdd(record, previousLocation.getIndex());
        // if (page.isOverfull()) {
        //     storageManager.splitPage(page);
        //     return key;
        // }
        return null; //placeholder
    }

    @Override
    public void delete(int key) {

    }
}
