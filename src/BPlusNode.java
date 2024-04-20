import java.util.List;
import java.nio.ByteBuffer;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.stream.Collectors;
public class BPlusNode {
    private boolean isLeaf;
    private boolean isRoot;
    private AttributeSchema attr;
    private int tableNumber;
    private int pageNumber;
    private int order;
    private LinkedList<Object> keys;
    private LinkedList<BPlusNode> children;
    private LinkedList<Pair<Integer, Integer>> pointers;
    private BPlusNode parent;

    public BPlusNode(int order, boolean isRoot, int tableNumber, AttributeSchema attr) {
        this.order = order;
        this.isRoot = isRoot;
        this.tableNumber = tableNumber;
        this.isLeaf = true;
        this.keys = new LinkedList<>();
        this.pointers = new LinkedList<>();
        this.parent = null;
        this.attr = attr;
        this.children = new LinkedList<BPlusNode>();
    }

    public Object insert(Record record, int searchKey, int pointer) {
        if(this.isLeaf) {
            for(int i = 0; i < keys.size(); i++) {
                Object key = keys.get(i);
                if (key != null) {
                    if (compare(searchKey, key) == 0) { //duplicate primarykey, cancel insert
                        System.err.println("Duplicate primarykey, insert cancelled");
                        return null;
                    }
                    if (compare(searchKey, key) < 0) { //insert key at this position
                        keys.add(i, searchKey);
                        System.out.println("adding: " + key);
                        return null;
                    }
                }
            }
            keys.add(searchKey);
            pointers.add(new Pair<Integer,Integer>(pointer, pointer));
            if(keys.size() == order) {
                int splitIndex = (int)Math.ceil(order/2);
                Object keyOnSplit = keys.get(splitIndex);
                BPlusNode LeafNode1 = new BPlusNode(order, false, 0, this.attr);
                BPlusNode LeafNode2 = new BPlusNode(order, false, 0, this.attr);
                LeafNode1.parent = this.parent;
                LeafNode2.parent = this.parent;

                LinkedList<Object> clonedKeys = new LinkedList<Object>();
                for(int i = splitIndex; i < keys.size(); i++) {
                    clonedKeys.add(keys.get(i));
                }
                keys.subList((int)splitIndex, keys.size()).clear();

                System.out.println("og list");
                for (Object object2 : keys) System.out.println(object2);
                System.out.println("new list");
                for (Object object3 : clonedKeys) System.out.println(object3);

                List<Pair<Integer, Integer>> clonedPointers = pointers.subList(splitIndex, pointers.size()).stream()
                        .map(Pair -> Pair).collect(Collectors.toList());
                pointers.subList(splitIndex, pointers.size()).clear();



                LeafNode1.keys = keys;
                LeafNode1.pointers = pointers;
                LeafNode2.keys = clonedKeys;
                LeafNode2.pointers = pointers;
                System.out.println(this.isRoot);
                if(this.isRoot) {
                    System.out.println("key on split: " + keyOnSplit);
                    this.keys = new LinkedList<Object>();
                    this.keys.add(keyOnSplit);
                    LeafNode1.parent = this;
                    LeafNode2.parent = this;
                    this.children.add(LeafNode1);
                    this.children.add(LeafNode2);
                    this.isLeaf = false;
                    return null;
                } else {
                    System.out.println();
                    parent.children.add(LeafNode1);
                    parent.children.add(LeafNode2);
                    parent.keys.add(keyOnSplit);
                    parent.children.remove(this);
                }

            }
        } else {
            BPlusNode childToInsert = null;
            for(BPlusNode child : this.children) {
                for(Object key : child.keys) {
                    if(compare(key, searchKey) < 0) {
                        childToInsert = child;
                    }
                }
            }
            childToInsert.insert(record, searchKey, pointer);
        }

        return null;

    }

    public void delete(int key) { return; }

    public int search(int key) { return -1; }

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
        for (BPlusNode child : children) {
            child.writeToFile();
        }
    }

    public void display() {
        this._display(0);
    }
    public void _display(int n) {
        System.out.println("level: " + n);
        for(Object key : this.keys) {
            System.out.print("| " + key);
        }
        System.out.print(" |\n");
        int level = n + 1;
        for(BPlusNode child : children) {
            child._display(level);
        }
    }

    public int compare(Object insertValue, Object existingValue) { //used for finding where to insert search keys
        if (attr.gettype().equalsIgnoreCase("integer")) {
            return (int)insertValue - (int)existingValue;
        }
        return 0; //placeholder value
    }

    static class Pair<K, V> {
        private final K pageNumber;
        private final V index;

        public Pair(K first, V second) {
            this.pageNumber = first;
            this.index = second;
        }

        public K getPageNumber() {
            return pageNumber;
        }

        public V getIndex() {
            return index;
        }
    }
}