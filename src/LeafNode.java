import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.nio.ByteBuffer;
import java.io.RandomAccessFile;

public class LeafNode extends Node {

    private LinkedList<Object> keys;
    private LinkedList<Pair<Integer, Integer>> pointers; //position of records in table file

    public LeafNode(int order, AttributeSchema attr, int tableNumber, boolean isRoot) {
        this.order = order;
        this.tableNumber = tableNumber;
        this.attr = attr;
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
            LeafNode newNode = new LeafNode(order, attr, tableNumber, false);
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

    @Override
    public void writeToFile() {
        ByteBuffer buffer = ByteBuffer.allocate(Main.getPageSize());
        buffer.putInt(keys.size()); // Amount of keys in node

        //add first pointer pair to buffer
        Pair<Integer, Integer> pointer = pointers.get(0);
        buffer.putInt(pointer.getPageNumber());
        buffer.putInt(pointer.getIndex());

        //add each key and remaining pointer to buffer
        for (int i = 0; i < keys.size(); i++) {
            Object key = keys.get(i);
            switch (attr.gettype().toLowerCase()) {
                case "varchar":
                    String varcharValue = (String) key;
                    byte[] varcharBytes = varcharValue.getBytes();
                    buffer.putInt(varcharValue.length());
                    buffer.put(varcharBytes);
                    break;
                case "char":
                    String charValue =  (String) key;
                    String paddedCharValue = String.format("%-" + attr.getsize() + "s", charValue);
                    byte[] charBytes = paddedCharValue.getBytes();
                    buffer.put(charBytes);
                    break;
                case "integer":
                    int intValue = (int) key;
                    buffer.putInt(intValue);
                    break;
                case "double":
                    double doubleValue = (double) key;
                    buffer.putDouble(doubleValue);
                    break;
                case "boolean":
                    boolean booleanValue = (boolean) key;
                    byte booleanByte = (byte) (booleanValue ? 1 : 0);
                    buffer.put(booleanByte);
                    break;
            }
            pointer = pointers.get(i+1);
            buffer.putInt(pointer.getPageNumber());
            buffer.putInt(pointer.getIndex());
        }

        // write buffer to file
        try {
            String fileName = Main.getDbDirectory() + "/indexFiles/" + tableNumber + ".bin";
            RandomAccessFile fileOut = new RandomAccessFile(fileName, "rw");
            int address = (pageNumber*Main.getPageSize()); //seek to page location in file
            fileOut.seek(address);
            fileOut.write(buffer.array());
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public boolean isFull() {
        if (this.isRoot && this.keys.size() == this.order - 1) {
            return true;
        } else { return this.keys.size() == this.order; }
    }
}
