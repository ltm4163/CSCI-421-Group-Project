import java.util.List;
import java.nio.ByteBuffer;
import java.io.RandomAccessFile;
import java.util.LinkedList;

public class InternalNode extends Node {

    private LinkedList<Object> keys;
    private LinkedList<Node> children;
    private LinkedList<Pair<Integer, Integer>> pointers; //position of children in file

    public InternalNode(int order, AttributeSchema attr, int tableNumber, boolean isRoot) {
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
    public Object insert(Record record, Object key, int pointer) {
        for(int i = 0; i < keys.size(); i++) {
            Object searchKey = keys.get(i);
            if (searchKey != null) {
                if(compare(key, searchKey) == 0) {
                    // TODO ???
                }
                if(compare(key, searchKey) < 0) {
                    // recurse insert down to child at corresponding pointer
                }
            }
        }
        return null;
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

        // have children write themselves to file
        for (Node child : children) {
            child.writeToFile();
        }
    }
}
