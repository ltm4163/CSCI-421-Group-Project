import java.util.List;
import java.nio.ByteBuffer;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.stream.Collectors;
public class BPlusNode {
    public boolean isLeaf;
    private boolean isRoot;
    private AttributeSchema attr;
    private int tableNumber;
    public int pageNumber;
    private int order;
    public LinkedList<Object> keys;
    public LinkedList<BPlusNode> children;
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

    public void insert(Record record, Object searchKey, int pointer) {
            System.out.println("Inserting into: ");
            for(Object key : this.keys) {
                System.out.print("| " + key);
            }
            System.out.println("|\n");
            if(this.isLeaf) {
                for (int i = 0; i < keys.size(); i++) {
                    Object key = keys.get(i);
                    if (key != null) {
                        if (compare(searchKey, key) == 0) { //duplicate primarykey, cancel insert
                            System.err.println("Duplicate primarykey, insert cancelled");
                            return;
                        }
                        if (compare(searchKey, key) < 0) { //insert key at this position
                            keys.add(i, searchKey);
                            System.out.println("adding: " + key);
                            return;
                        }
                    }
                }
                keys.add(searchKey);
                pointers.add(new Pair<Integer, Integer>(pointer, pointer));
                if (keys.size() == order) {
                    int splitIndex = (int) Math.ceil(order / 2);
                    Object keyOnSplit = keys.get(splitIndex);
                    BPlusNode LeafNode1 = new BPlusNode(order, false, 0, this.attr); // TODO fix table number for these two
                    BPlusNode LeafNode2 = new BPlusNode(order, false, 0, this.attr);
                    LeafNode1.parent = this.parent;
                    LeafNode2.parent = this.parent;

                    LinkedList<Object> clonedKeys = new LinkedList<Object>();
                    for (int i = splitIndex; i < keys.size(); i++) {
                        clonedKeys.add(keys.get(i));
                    }
                    keys.subList((int) splitIndex, keys.size()).clear();

                    List<Pair<Integer, Integer>> clonedPointers = pointers.subList(splitIndex, pointers.size()).stream()
                            .map(Pair -> Pair).collect(Collectors.toList());
                    pointers.subList(splitIndex, pointers.size()).clear();


                    LeafNode1.keys = keys;
                    LeafNode1.pointers = pointers;
                    LeafNode2.keys = clonedKeys;
                    LeafNode2.pointers = pointers;
                    if (this.isRoot) {
                        System.out.println("key on split: " + keyOnSplit);
                        this.keys = new LinkedList<Object>();
                        this.insertInternal(keyOnSplit, pointer);
                        LeafNode1.parent = this;
                        LeafNode2.parent = this;
                        this.children.add(LeafNode1);
                        this.children.add(LeafNode2);
                        this.isLeaf = false;
                    } else {
                        System.out.println();
                        System.out.println("Parent");
                        parent.display();
                        parent.children.add(LeafNode1);
                        parent.children.add(LeafNode2);
                        parent.insertInternal(keyOnSplit, 0);
                        parent.children.remove(this);
                    }

                }
            } else {
                for(int i = 0; i < keys.size(); i++) {
                    if(compare(keys.get(i),searchKey) < 0) {
                        children.get(i).insert(record,searchKey,pointer);
                    }
                }
            }


    }

    public void insertInternal(Object searchKey, int pointer) {
        System.out.println("InternalInserting into: ");
        for(Object key : this.keys) {
            System.out.print("| " + key);
        }
        System.out.println("|\n");

        for (int i = 0; i < keys.size(); i++) {
            Object key = keys.get(i);
            if (key != null) {
                if (compare(searchKey, key) == 0) { //duplicate primarykey, cancel insert
                    System.err.println("Duplicate primarykey, insert cancelled");
                    return;
                }
                if (compare(searchKey, key) < 0) { //insert key at this position
                    keys.add(i, searchKey);
                    System.out.println("adding: " + key);
                    return;
                }
            }
        }
        keys.add(searchKey);
        pointers.add(new Pair<Integer, Integer>(pointer, pointer));

        if (keys.size() == order) {
            int splitIndex = (int) Math.ceil(order / 2);
            Object keyOnSplit = keys.get(splitIndex);
            BPlusNode LeafNode1 = new BPlusNode(order, false, 0, this.attr); // TODO fix table number for these two
            BPlusNode LeafNode2 = new BPlusNode(order, false, 0, this.attr);
            LeafNode1.parent = this.parent;
            LeafNode2.parent = this.parent;

            LinkedList<Object> clonedKeys = new LinkedList<Object>();
            for (int i = splitIndex; i < keys.size(); i++) {
                clonedKeys.add(keys.get(i));
            }
            keys.subList((int) splitIndex, keys.size()).clear();

            List<Pair<Integer, Integer>> clonedPointers = pointers.subList(splitIndex, pointers.size()).stream()
                    .map(Pair -> Pair).collect(Collectors.toList());
            pointers.subList(splitIndex, pointers.size()).clear();


            LeafNode1.keys = keys;
            LeafNode1.pointers = pointers;
            LeafNode2.keys = clonedKeys;
            LeafNode2.pointers = pointers;
            if (this.children.size() > 0) {
                for (int i = 0; i < splitIndex + 1; i++) {
                    this.children.get(i).parent = LeafNode1;
                    LeafNode1.children.add(this.children.get(i));
                }
                for (int i = splitIndex + 1; i < this.children.size(); i++) {
                    this.children.get(i).parent = LeafNode2;
                    LeafNode2.children.add(this.children.get(i));
                }
                LeafNode1.isLeaf = false;
                LeafNode2.isLeaf = false;
                this.children = new LinkedList<BPlusNode>();
            }
            if (this.isRoot) {
                System.out.println("key on split: " + keyOnSplit);
                this.keys = new LinkedList<Object>();
                this.insertInternal(keyOnSplit, 0);
                LeafNode1.parent = this;
                LeafNode2.parent = this;
                LeafNode2.keys.remove(keyOnSplit);
                this.children.add(LeafNode1);
                this.children.add(LeafNode2);
                this.isLeaf = false;
            } else {
                System.out.println();
                LeafNode2.keys.remove(keyOnSplit);
                parent.children.add(LeafNode1);
                parent.children.add(LeafNode2);
                parent.insertInternal(keyOnSplit, 0);
                parent.children.remove(this);
            }

        }
    }

    public void delete(int key) { return; }

    public int search(int key) { return -1; }

    public BPlusNode findLeafToInsert(Object searchKey) {
        if(this.isLeaf) {
            return this;
        }
        for(int i = 0; i < keys.size(); i++) {
            if(compare(searchKey, keys.get(i)) < 0) {
                System.out.println("here");
                return this.children.get(i).findLeafToInsert(searchKey);
            }
        }
        return null;
    }

    public int search(Object key) {
        if (isLeaf) {
            for (int i = 0; i < keys.size(); i++) {
                // If the key is found in the current leaf node...
                if (compare(key, keys.get(i)) == 0) {
                    Pair<Integer, Integer> pointer = pointers.get(i);
                    return pointer.getIndex();  // Return index/pointer value
                }
            }
            return -1;  // Key not found in leaf node
        }
        else {  // If not leaf, traverse to appropriate child node
            BPlusNode childNode = getChildNodeForKey(key);
            if (childNode != null) {
                return childNode.search(key);  // Recursively search in child node
            }
        }
        return -1;  // Key not found in tree
    }

    // Gets the appropriate child node for a given search key
    private BPlusNode getChildNodeForKey(Object key) {
        for (int i = 0; i < keys.size(); i++) {
            if (i == 0 && compare(key, keys.get(i)) < 0) {
                // Key is less than first key, choose leftmost child
                return children.get(i);
            }
            else if (i == keys.size() - 1 && compare(key, keys.get(i)) >= 0) {
                // Key is greater than or equal to last key, choose the rightmost child
                return children.get(i + 1);
            }
            else if (i > 0 && compare(key, keys.get(i - 1)) >= 0 && compare(key, keys.get(i)) < 0) {
                // Key is between two keys, choose the corresponding child
                return children.get(i);
            }
        }
        return null; // Should never reach here
    }

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
        if(this.isRoot) { System.out.print("root: ");}
        if(this.isLeaf) { System.out.print("leaf: "); }
        else { System.out.print("internal: "); }
        for(Object key : this.keys) {
            System.out.print("| " + key);
        }

        System.out.print(" |\n");
        for(BPlusNode child : children) {
            child.display();
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