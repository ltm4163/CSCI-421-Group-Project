import java.util.ArrayList;
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
        ArrayList<Integer> freeSpaces = tableSchema.getFreeSpaces();
        if (freeSpaces.size() > 0) this.pageNumber = freeSpaces.remove(0);
        else this.pageNumber = tableSchema.getNumNodes();
        tableSchema.addTreeNode();
        this.parent = null;
        this.attr = attr;
        this.children = new LinkedList<BPlusNode>();
        this.keys = new LinkedList<>();
        this.pointers = new LinkedList<>();
    }

    // increments index of subsequent pointers sharing same pagenumber as added record
    // returns true if ended early, false otherwise
    public boolean incrementPointerIndexes(int pageNum, int indexInPointerList) {
        for (int i = indexInPointerList; i < pointers.size(); i++) {
            Pair<Integer, Integer> pointer = pointers.get(i);
            if (pointer.getPageNumber() == pageNum) pointers.set(i, new Pair<Integer,Integer>(pointer.getPageNumber(), pointer.getIndex()+1));
            else return true;
        }
        return false;
    }

    /*
     * BPlusTree insert function
     *
     * @param Record record    record to insert
     * @param Object searchKey  key to search on
     * @param int pointer       is this needed...?
     * @param boolean intoInternal  indicates if inserting into internal node
     */
    public boolean insert(Record record, Object searchKey, int pointer, boolean intoInternal) {//, ArrayList<BPlusNode> leafNodes) {
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
                            System.out.println(searchKey + " < " + key);
                            if (isLeaf) { // if leaf node, insert record into table, add pointer to record and adjust following pointer
                                Pair<Integer, Integer> nextPointer = pointers.get(i);
                                System.out.println("npagenum: " + nextPointer.getPageNumber() + ", nindex: " + nextPointer.getIndex());
                                StorageManager storageManager = Main.getStorageManager();
                                Page page = storageManager.getPage(tableNumber, nextPointer.getPageNumber());
                                page.shiftRecordsAndAdd(record, nextPointer.getIndex());
                                pointers.set(i, new Pair<Integer, Integer>(nextPointer.pageNumber, nextPointer.index+1));
                                pointers.add(i, new Pair<Integer, Integer>(nextPointer.pageNumber, nextPointer.index));

                                if (pointers.size() >= i+2) incrementPointerIndexes(nextPointer.getPageNumber(), i+2);
                                BPlusNode rightNeighbor = getRightSibling();
                                while (rightNeighbor != null) {
                                    if(!rightNeighbor.incrementPointerIndexes(nextPointer.getPageNumber(), 0)) {
                                        rightNeighbor = rightNeighbor.getRightSibling();
                                    }
                                }
                                // get rightneighbors and incrementpointerindexes until reach next page in table

                                if (page.isOverfull()) { // if page overfull, split page and adjust b+tree pointers
                                    Record firstRecInNewPage = storageManager.splitPage(page);
                                    AttributeSchema[] attributeSchemas = Main.getCatalog().getTableSchema(tableNumber).getattributes();
                                    Object firstValInNewPage = firstRecInNewPage.getAttributeValue(attr.getname(), attributeSchemas);

//                                    for (BPlusNode node : leafNodes) {
//                                        for (int j = 0; j < node.pointers.size(); j++) {
//                                            Pair<Integer, Integer> pointerForAdjustment = node.pointers.get(j);
//                                            if (pointerForAdjustment.getPageNumber() >= page.getPageNumber()) {
//                                                pointerForAdjustment.setPageNumber(pointerForAdjustment.getPageNumber() + 1);
//                                            }
//                                            else if (pointerForAdjustment.getPageNumber() == page.getPageNumber()) {
//                                                Object keyForAdjustment = node.keys.get(i);
//                                                if (compare(keyForAdjustment, firstValInNewPage) >= 0) {
//                                                    pointerForAdjustment.setPageNumber(pointerForAdjustment.getPageNumber() + 1);
//                                                }
//                                            }
//                                        }
//                                    }
                                    //TODO: adjust pointers accordingly
                                    // go through each leaf node. for each entry, check if its pagenum (in table, NOT tree) >= pagenum
                                    // of split page. if >, increase its pagenum of pointer by 1. if =, check if searchkey value >
                                    // firstValInNewPage. if it is, increase pagenum of pointer by 1 and update index (this part im
                                    // not sure exactly how to do, I imagine you just set the first changed one to 0 and increment
                                    // from there though)
                                    // EDIT: I think we also need to figure out the pointers for the parent node on a split
                                }
                            }
                            System.out.println("adding: " + searchKey);
                            return true;
                        }
                    }
                }
                System.out.println("end: " + searchKey);
                keys.add(searchKey);
                if (isLeaf) {// if leaf node, insert record into table, add pointer to record
                    if (pointers.isEmpty()) { // create page if first entry in table
                        Page newPage = new Page(0, tableNumber, true);
                        newPage.addRecord(record);
                        Main.getCatalog().getTableSchema(tableNumber).addPage(newPage);
                        //Main.getBuffer().addPage(newPage.getPageNumber(), newPage);
                        pointers.add(new Pair<Integer,Integer>(0, 0));
                    }
                    else {
                        Pair<Integer, Integer> prevPointer = pointers.getLast();
                        System.out.println("prpagenum: " + prevPointer.getPageNumber() + ", prindex: " + prevPointer.getIndex());
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
                        this.insert(record, keyOnSplit, pointer, true);//, leafNodes);
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
                        parent.insert(record, keyOnSplit, 0, true);//, leafNodes);
                        //parent.pointers.remove(Pair<Integer, Integer>(this.pageNumber, -1));
                        parent.children.remove(this);
                        Main.getCatalog().getTableSchema(tableNumber).deleteTreeNode();
                    }

                }
            } else {
                return search(searchKey).insert(record, searchKey, pointer, false);//, leafNodes);
            }
            return true;
    }

    /*
     * BPlusTree delete function
     *
     * @param Object searchKey  key to delete
     * @param intoInternal      Indicates if the key is being deleted from an intenral node
     */

    public void delete(Object searchKey, boolean intoInternal) {
         if(isLeaf || intoInternal) {
             // FOR BUG TESTING

             // removes the key from the node
            int index = keys.indexOf(searchKey);
            keys.remove(searchKey);
            Pair<Integer, Integer> pointer = pointers.remove(index);
            Page page = Main.getStorageManager().getPage(tableNumber, pointer.getPageNumber());
            Record record = page.getRecords().get(pointer.getIndex());
            page.deleteRecord(record, pointer.getIndex());
            if (page.getNumRecords() == 0) Main.getCatalog().getTableSchema(tableNumber).dropPage(page.getPageNumber());
             // if the node becomes underfull, borrow. If you can't borrow, merge.
            if(children.size() > (int)Math.ceil(order/2) || (isRoot && children.size() == 0)) {
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

    // returns the right sibling of a node
    public BPlusNode getRightSibling() {
        if (parent == null) return null;
        int index = parent.children.indexOf(this);
        if(index + 1 < parent.children.size()) {
            return parent.children.get(index + 1);
        }
        return null;

    }

    public BPlusNode getRightSiblingInclusive() {
        if (parent == null) return null;
        int index = parent.children.indexOf(this);
        if(index + 1 < parent.children.size()) {
            return parent.children.get(index + 1);
        } else {
            return parent.getRightSiblingInclusive().children.get(0);
        }

    }

    // returns the left sibling of a node
    public BPlusNode getLeftSibling() {
        if (parent == null) return null;
        int index = parent.children.indexOf(this);
        if(index - 1 >= 0) {
            return parent.children.get(index - 1);
        }
        return null;
    }

    public BPlusNode getLeftSiblingInclusive() {
        if (parent == null) return null;
        int index = parent.children.indexOf(this);
        if(index - 1 >= 0) {
            return parent.children.get(index - 1);
        } else {
            int size = parent.getRightSiblingInclusive().children.size();
            return parent.getLeftSiblingInclusive().children.get(size - 1);
        }
    }

    // merges a node with its right sibling
    public boolean mergeRight() {
        int index = parent.children.indexOf(this);
        BPlusNode nodeToMerge = this.getRightSibling();
        // creates a new node for the merged node
        BPlusNode merged = new BPlusNode(order, isRoot, tableNumber, attr);
        // if a right sibling wasn't found, look left
        if (nodeToMerge == null) { return false; }
        // insert keys into the merged node
        for(Object key : this.keys) {
            merged.insert(null, key, 0, true);
        }
        for(Object key : nodeToMerge.keys) {
            merged.insert(null, key, 0, true);
        }
        // add children of both nodes to the merged node
        merged.children.addAll(this.children);
        merged.children.addAll(nodeToMerge.children);
        merged.parent = parent;
        parent.children.remove(this);
        parent.children.remove(nodeToMerge);
        parent.children.add(index, merged);

        // TODO may need to modify this
        if(parent.children.size() == 1 && parent.isRoot) {
            merged.isRoot = true;
            merged.parent = null;
            merged.isLeaf = false;
            return true;
        }
        // deletes the key from the parent node
        parent.delete(parent.keys.get(index), true);
        return true;
    }

    // merges a node with its left sibling, essentially follows the same logic as merge right
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
        // TODO may need to modify this
        if(parent.children.size() == 1 && parent.isRoot) {
            merged.isRoot = true;
            merged.parent = null;
            merged.isLeaf = false;
            return true;
        }
        parent.delete(parent.keys.get(index), true);
        return true;
    }

    // merge function
    public void merge() {
        if(mergeLeft())
            return;
        if(mergeRight())
            return;
    }

    // borrows a key, pointer pair from a sibling
    public boolean borrowFrom() {
        BPlusNode toBorrowFrom = null;
        Object key = null;
        // case where node is root
        // TODO may need to generalize this
        if(isRoot) {
            toBorrowFrom = children.get(children.size() - 1);
            key = toBorrowFrom.keys.get(0);
        } else if(parent.isRoot) { // if parent is root, borrow from root
            toBorrowFrom = parent;
            key = parent.keys.get(0);
        } else if(getLeftSibling() != null) { // borrow from left sibling
            toBorrowFrom = getLeftSibling();
            key = toBorrowFrom.keys.get(toBorrowFrom.keys.size() - 1);
            toBorrowFrom.isRoot = false;
        } else if(getRightSibling() != null) { // borrow from right sibling
            toBorrowFrom = getRightSibling();
            key = toBorrowFrom.keys.get(0);
        }
        // TODO fix size to reflect actual capacity
        if(toBorrowFrom == null || toBorrowFrom.keys.size() <= (int)Math.ceil(order/2)) {
            return false;
        }
        // TODO pointer redistribution, check that this works
        if(isRoot) {
            BPlusNode nodeToRedistribute = toBorrowFrom.children.get(toBorrowFrom.keys.indexOf(key));
            toBorrowFrom.children.remove(nodeToRedistribute);
            children.get(children.indexOf(toBorrowFrom) - 1).children.add(nodeToRedistribute);

        }
        insert(null, key, 0, !isLeaf);
        toBorrowFrom.delete(key, !toBorrowFrom.isLeaf);
        return true;
    }

    public void update(Object keyToUpdate, Object key) {
        delete(keyToUpdate, false);
        insert(null, key, 0, false);
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

    public ArrayList<BPlusNode> getLeafNodes() {
        ArrayList<BPlusNode> leafNodes = new ArrayList<>();
        collectLeafNodes(this, leafNodes);
        return leafNodes;
    }

    private void collectLeafNodes(BPlusNode node, ArrayList<BPlusNode> leafNodes) {
        if (node.isLeaf) {
            leafNodes.add(node);
        }
        else {
            for (BPlusNode child : node.children) {
                collectLeafNodes(child, leafNodes);
            }
        }
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
        private K pageNumber;
        private final V index;

        public Pair(K pageNumber, V index) {
            this.pageNumber = pageNumber;
            this.index = index;
        }

        public K getPageNumber() {
            return pageNumber;
        }

        public void setPageNumber(K value) {
            pageNumber = value;
        }

        public V getIndex() {
            return index;
        }
    }
}