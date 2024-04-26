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
        this.isLeaf = true;
        this.tableNumber = tableNumber;
        this.parent = null;
        this.attr = attr;
        this.children = new LinkedList<BPlusNode>();
        this.keys = new LinkedList<>();
        this.pointers = new LinkedList<>();
    }

    /*
     * BPlusTree insert function
     *
     * @param Record record    record to insert
     * @param Object searchKey  key to search on
     * @param int pointer       is this needed...?
     * @param boolean intoInternal  indicates if inserting into internal node
     */
    public void insert(Record record, Object searchKey, int pointer, boolean intoInternal) {
            // if inserting into leaf or internal node
            if(isLeaf || intoInternal) {
                // insert key into node
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
                // TODO fix this
                // if(intoInternal) { pointers.add(new Pair<page number, -1>); }
                // else { pointers.add(new Pair<Integer, Integer>(pointer, pointer)); }
                // if keys become overfull, split
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

                    // are pointers even needed with this setup?
                    List<Pair<Integer, Integer>> clonedPointers = pointers.subList(splitIndex, pointers.size()).stream()
                            .map(Pair -> Pair).collect(Collectors.toList());
                    pointers.subList(splitIndex, pointers.size()).clear();


                    LeafNode1.keys = keys;
                    LeafNode1.pointers = pointers;
                    LeafNode2.keys = clonedKeys;
                    LeafNode2.pointers = pointers;
                    // if the node has children, it's children must be split among the new nodes
                    if (this.children.size() > 0) {
                        for (int i = 0; i < splitIndex + 1; i++) {
                            children.get(i).parent = LeafNode1;
                            LeafNode1.children.add(children.get(i));
                        }
                        for (int i = splitIndex + 1; i < children.size(); i++) {
                            children.get(i).parent = LeafNode2;
                            LeafNode2.children.add(children.get(i));
                        }
                        LeafNode1.isLeaf = false;
                        LeafNode2.isLeaf = false;
                        this.children = new LinkedList<BPlusNode>();
                    }
                    if (this.isRoot) {
                        keys = new LinkedList<Object>();
                        this.insert(record, keyOnSplit, pointer, true);
                        LeafNode1.parent = this;
                        LeafNode2.parent = this;
                        // idk why this needs to be done but it works
                        if(intoInternal) { LeafNode2.keys.remove(keyOnSplit);}
                        children.add(LeafNode1);
                        children.add(LeafNode2);
                        isLeaf = false;
                    } else {
                        if(intoInternal) { LeafNode2.keys.remove(keyOnSplit);}
                        parent.children.add(LeafNode1);
                        parent.children.add(LeafNode2);
                        parent.insert(record, keyOnSplit, 0, true);
                        parent.children.remove(this);
                    }

                }
            } else {
                // TODO make searching work without, use search function?
                search(searchKey).insert(record, searchKey, pointer, false);
            }


    }

    public void delete(Object searchKey, boolean intoInternal) {
         if(isLeaf || intoInternal) {
             // FOR BUG TESTING

            keys.remove(searchKey);
            // TODO remove pointer
            if(children.size() > Math.ceil(order/2) || (isRoot && children.size() == 0)) {
                if(borrowFrom()) {
                    return;
                }
                if(isRoot) {
                    children.get(0).merge();

                } else { merge(); }

            }
        } else {
            search(searchKey).delete(searchKey, false);
        }

    }

    public BPlusNode getRightSibling() {
        int index = parent.children.indexOf(this);
        if(index + 1 < parent.children.size()) {
            return parent.children.get(index + 1);
        }
        return null;
    }

    public BPlusNode getLeftSibling() {
        int index = parent.children.indexOf(this);
        if(index - 1 >= 0) {
            return parent.children.get(index - 1);
        }
        return null;
    }


    public boolean mergeRight() {
        int index = parent.children.indexOf(this);
        BPlusNode nodeToMerge = this.getRightSibling();
        BPlusNode merged = new BPlusNode(order, isRoot, tableNumber, attr);
        if (nodeToMerge == null) { return false; }
        for(Object key : this.keys) {
            merged.insert(null, key, 0, true);
        }
        for(Object key : nodeToMerge.keys) {
            merged.insert(null, key, 0, true);
        }
        merged.children.addAll(this.children);
        merged.children.addAll(nodeToMerge.children);
        merged.parent = parent;
        parent.children.remove(this);
        parent.children.remove(nodeToMerge);
        parent.children.add(index, merged);
        if(parent.children.size() == 1) {
            merged.isRoot = true;
            merged.parent = null;
            merged.isLeaf = false;
            return true;
        }
        parent.delete(parent.keys.get(index), true);
        return true;
    }

    public boolean mergeLeft() {
        BPlusNode nodeToMerge = this.getLeftSibling();
        int index = parent.children.indexOf(nodeToMerge);
        BPlusNode merged = new BPlusNode(order, isRoot, tableNumber, attr);
        if (nodeToMerge == null) { return false; }
        for(Object key : this.keys) {
            merged.insert(null, key, 0, true);
        }
        for(Object key : nodeToMerge.keys) {
            merged.insert(null, key, 0, true);
        }
        merged.children.addAll(this.children);
        merged.children.addAll(nodeToMerge.children);
        merged.parent = parent;
        parent.children.remove(this);
        parent.children.remove(nodeToMerge);
        parent.children.add(index, merged);
        if(parent.children.size() == 1) {
            merged.isRoot = true;
            merged.parent = null;
            merged.isLeaf = false;
            return true;
        }
        parent.delete(parent.keys.get(index), true);
        return true;
    }




    public void merge() {
        if(mergeLeft())
            return;
        if(mergeRight())
            return;
    }

    public boolean borrowFrom() {
        BPlusNode toBorrowFrom = null;
        Object key = null;
        if(isRoot) {
            toBorrowFrom = children.get(children.size() - 1);
            key = toBorrowFrom.keys.get(0);
        } else if(parent.isRoot) {
            toBorrowFrom = parent;
            key = parent.keys.get(0);
        } else if(getLeftSibling() != null) {
            toBorrowFrom = getLeftSibling();
            key = toBorrowFrom.keys.get(toBorrowFrom.keys.size() - 1);
            toBorrowFrom.isRoot = false;
        } else if(getRightSibling() != null) {
            toBorrowFrom = getRightSibling();
            key = toBorrowFrom.keys.get(0);
        }
        // TODO fix size to reflect actual capacity
        if(toBorrowFrom == null || toBorrowFrom.keys.size() == 2) {
            return false;
        }
        if(isRoot) {
            BPlusNode nodeToRedistribute = toBorrowFrom.children.get(toBorrowFrom.keys.indexOf(key));
            toBorrowFrom.children.remove(nodeToRedistribute);
            children.get(children.indexOf(toBorrowFrom) - 1).children.add(nodeToRedistribute);

        }
        insert(null, key, 0, !isLeaf);
        toBorrowFrom.delete(key, !toBorrowFrom.isLeaf);
        return true;
    }

    public LinkedList<BPlusNode> getChildren() {
        return this.children;
    }

    public LinkedList<Object> getKeys() {
        return this.keys;
    }

    public boolean isLeaf() {
        return this.isLeaf;
    }

//    public int search(Object key) {
//        if (isLeaf) {
//            for (int i = 0; i < keys.size(); i++) {
//                // If the key is found in the current leaf node...
//                if (compare(key, keys.get(i)) == 0) {
//                    Pair<Integer, Integer> pointer = pointers.get(i);
//                    return pointer.getIndex();  // Return index/pointer value
//                }
//            }
//            return -1;  // Key not found in leaf node
//        }
//        else {  // If not leaf, traverse to appropriate child node
//            BPlusNode childNode = getChildNodeForKey(key);
//            if (childNode != null) {
//                return childNode.search(key);  // Recursively search in child node
//            }
//        }
//        return -1;  // Key not found in tree
//    }

    public BPlusNode search(Object key) {
        if (!isLeaf) {
            BPlusNode childNode = getChildNodeForKey(key);
            if (childNode != null) {
                if (childNode.isLeaf) {
                    return childNode;
                }
                return childNode.search(key);
            }
        }
        return null;  // Key not found in tree
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