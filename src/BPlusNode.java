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
        TableSchema tableSchema = Main.getCatalog().getTableSchema(tableNumber);
        this.pageNumber = tableSchema.getNumNodes();
        tableSchema.addTreeNode();
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
    public boolean insert(Record record, Object searchKey, int pointer, boolean intoInternal) {
            // if inserting into leaf or internal node
            if(isLeaf || intoInternal) {
                // insert key into node
                for (int i = 0; i < keys.size(); i++) {
                    Object key = keys.get(i);
                    if (key != null) {
                        if (compare(searchKey, key) == 0) { //duplicate primarykey, cancel insert
                            System.err.println("Duplicate primarykey, insert cancelled");
                            return false;
                        }
                        if (compare(searchKey, key) < 0) { //insert key at this position
                            keys.add(i, searchKey);
                            if (isLeaf) { // if leaf node, insert record into table, add pointer to record and adjust following pointer
                                Pair<Integer, Integer> nextPointer = pointers.get(i);
                                StorageManager storageManager = Main.getStorageManager();
                                Page page = storageManager.getPage(tableNumber, nextPointer.getPageNumber());
                                page.shiftRecordsAndAdd(record, nextPointer.getIndex());
                                pointers.set(i, new Pair<Integer, Integer>(nextPointer.pageNumber, nextPointer.index+1));
                                pointers.add(i, new Pair<Integer, Integer>(nextPointer.pageNumber, nextPointer.index));
                                if (page.isOverfull()) { // if page overfull, split page and adjust b+tree pointers
                                    Record firstRecInNewPage = storageManager.splitPage(page);
                                    AttributeSchema[] attributeSchemas = Main.getCatalog().getTableSchema(tableNumber).getattributes();
                                    Object firstValInNewPage = firstRecInNewPage.getAttributeValue(attr.getname(), attributeSchemas);
                                    //TODO: adjust pointers accordingly
                                    // go through each leaf node. for each entry, check if its pagenum (in table, NOT tree) >= pagenum
                                    // of split page. if >, increase its pagenum of pointer by 1. if =, check if searchkey value >
                                    // firstValInNewPage. if it is, increase pagenum of pointer by 1 and update index (this part im
                                    // not sure exactly how to do, I imagine you just set the first changed one to 0 and increment 
                                    // from there though)
                                }
                            }
                            System.out.println("adding: " + key);
                            return true;
                        }
                    }
                }
                keys.add(searchKey);
                if (isLeaf) {// if leaf node, insert record into table, add pointer to record
                    if (pointers.isEmpty()) { // create page if first entry in table
                        Page newPage = new Page(0, tableNumber, true);
                        newPage.addRecord(record);
                        Main.getCatalog().getTableSchema(tableNumber).addPage(newPage);
                        //Main.getBuffer().addPage(newPage.getPageNumber(), newPage);
                        pointers.add(new Pair<Integer,Integer>(0, 0));
                    }
                    else { // adjusts previous pointer
                        Pair<Integer, Integer> prevPointer = pointers.getLast();
                        StorageManager storageManager = Main.getStorageManager();
                        Page page = storageManager.getPage(tableNumber, prevPointer.getPageNumber());
                        page.addRecord(record);
                        pointers.add(new Pair<Integer, Integer>(prevPointer.pageNumber, prevPointer.index+1));
                        if (page.isOverfull()) {
                            storageManager.splitPage(page);
                            //TODO: adjust pointers accordingly
                        }
                    }
                }
                // TODO fix this
                // if(intoInternal) { pointers.add(new Pair<page number, -1>); }
                // else { pointers.add(new Pair<Integer, Integer>(pointer, pointer)); }
                // if keys become overfull, split
                if (keys.size() == order) {
                    int splitIndex = (int) Math.ceil(order / 2);
                    Object keyOnSplit = keys.get(splitIndex);
                    BPlusNode LeafNode1 = new BPlusNode(order, false, tableNumber, this.attr);
                    BPlusNode LeafNode2 = new BPlusNode(order, false, tableNumber, this.attr);
                    LeafNode1.parent = this.parent;
                    LeafNode2.parent = this.parent;

                    LinkedList<Object> clonedKeys = new LinkedList<Object>();
                    for (int i = splitIndex; i < keys.size(); i++) {
                        clonedKeys.add(keys.get(i));
                    }
                    keys.subList((int) splitIndex, keys.size()).clear();

                    LeafNode1.keys = keys;
                    LeafNode2.keys = clonedKeys;
                    // if the node has children, it's children must be split among the new nodes
                    if (this.children.size() > 0) {
                        for (int i = 0; i < splitIndex + 1; i++) {
                            BPlusNode child = children.get(i);
                            child.parent = LeafNode1;
                            LeafNode1.children.add(child);
                            LeafNode1.pointers.add(pointers.get(i));
                        }
                        for (int i = splitIndex + 1; i < children.size(); i++) {
                            BPlusNode child = children.get(i);
                            child.parent = LeafNode2;
                            LeafNode2.children.add(child);
                            LeafNode2.pointers.add(pointers.get(i));
                        }
                        LeafNode1.isLeaf = false;
                        LeafNode2.isLeaf = false;
                        this.children = new LinkedList<BPlusNode>();
                    }
                    else { // if leaf node, copy pointers
                        LinkedList<Pair<Integer, Integer>> clonedPointers = new LinkedList<>();
                        for (int i = splitIndex; i < pointers.size(); i++) {
                            clonedPointers.add(pointers.get(i));
                        }
                        pointers.subList((int) splitIndex, pointers.size()).clear();
                        LeafNode2.pointers = clonedPointers;
                        LeafNode1.pointers = pointers;
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
                        pointers = new LinkedList<>();
                        pointers.add(new Pair<Integer,Integer>(LeafNode1.pageNumber, -1));
                        pointers.add(new Pair<Integer,Integer>(LeafNode2.pageNumber, -1));
                        isLeaf = false;
                    } else {
                        if(intoInternal) { LeafNode2.keys.remove(keyOnSplit);}
                        parent.children.add(LeafNode1);
                        parent.children.add(LeafNode2);
                        parent.pointers.add(new Pair<Integer,Integer>(LeafNode1.pageNumber, -1));
                        parent.pointers.add(new Pair<Integer,Integer>(LeafNode2.pageNumber, -1));
                        parent.insert(record, keyOnSplit, 0, true);
                        //parent.pointers.remove(Pair<Integer, Integer>(this.pageNumber, -1));
                        parent.children.remove(this);
                        Main.getCatalog().getTableSchema(tableNumber).deleteTreeNode();
                    }

                }
            } else {
                // TODO make searching work without, use search function?
                return search(searchKey).insert(record, searchKey, pointer, false);
            }
            return true;
    }

    public void delete(Object searchKey, boolean intoInternal) {
        if(isRoot && intoInternal) {
            keys.remove(searchKey);
            if(keys.size() == 0) {
                keys.add(children.get(1).keys.get(0));
                children.get(1).keys.remove(0);
            }
            return;
        }
        if(isLeaf || intoInternal) {
            if(intoInternal) { System.out.println("deleting from internal"); }
            keys.remove(searchKey);
            // TODO remove pointer
            if(keys.size() == 1) {
                BPlusNode merged = new BPlusNode(order, false, tableNumber, attr); // TODO what is tablenumber
                BPlusNode nodeToMerge = null;
                int index = parent.getChildren().indexOf(this);
                if(parent.isRoot) {
                    nodeToMerge = parent;
                    merged.keys.addAll(this.keys);
                    merged.keys.addAll(nodeToMerge.keys);
                } else {
                    if(index - 1 >= 0) {
                        nodeToMerge = parent.getChildren().get(index - 1);
                    } else {
                        nodeToMerge = parent.getChildren().get(index + 1);
                    }
                    merged.keys.addAll(nodeToMerge.keys);
                    merged.keys.addAll(this.keys);
                 }
                parent.delete(searchKey, true);
                parent.delete(this.keys.get(0), true);
                parent.children.remove(this);
                parent.children.remove(nodeToMerge);
                if(intoInternal) {
                    System.out.println("here");
                    merged.isLeaf = false;
                    merged.children = this.children;
                }
                if(index - 1 > 0) {
                    parent.children.add(index - 1, merged);
                } else { parent.children.add(0, merged); }
                merged.parent = this.parent;
            }
        } else {
            search(searchKey).delete(searchKey, false);
        }

    }

    public int getPageNumber() {
        return pageNumber;
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
            return ((Integer) insertValue).compareTo((Integer) existingValue);
        }
        else if (attr.gettype().equalsIgnoreCase("double")) {
            return ((Double) insertValue).compareTo((Double) existingValue);
        }
        else {
            return ((String) insertValue).compareTo((String) existingValue);
        }
    }

    static class Pair<K, V> {
        private final K pageNumber;
        private final V index;

        public Pair(K pageNumber, V index) {
            this.pageNumber = pageNumber;
            this.index = index;
        }

        public K getPageNumber() {
            return pageNumber;
        }

        public V getIndex() {
            return index;
        }
    }
}