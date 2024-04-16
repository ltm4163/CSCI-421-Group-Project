public class BPlusTree {

    private Node root;
    private int order;  // N value

    // default constructor

    public BPlusTree(int order, String attrType, int tableNumber) {
        this.root = new LeafNode(order, attrType, tableNumber, true);
        this.order = order;
    }

    public int search(int key) {
        return root.search(key);
    }

    private boolean isEmpty() {
        return this.root == null;
    }

    /*
     * Root level implementation of insert function
     * Initially, the root should be a leaf node, in which case it can be
     * inserted into directly. If it becomes overfull, split. If it is an
     * internal node, search through the tree.
     *
     * @param Record record    record to insert
     * @param int key          key value
     * @param int pointer      pointer value
     */
    public void insert(Record record, int key, int pointer) {
        // if the B+Tree is completely empty, insert as new leaf
        if(isEmpty()) {
            // TODO set root to new leafnode?
        } else if (root instanceof LeafNode) {
            // if B+ root is a leaf, treat as leaf
            LeafNode rootlf = (LeafNode) root;
            if(rootlf.isFull()) {
                int mid = (int) Math.ceil(this.order / 2.0) - 1;
                // construct child1 as leaf node with values from 0 to mid
                // construct child2 as leaf node with values from mid+1 to len(keys)
                // construct new root as internal node with children child1, child2, key = key at mid
                // set root to new root node
            } else {
                rootlf.insert(record, key, pointer);
                // this.root = rootlf?
            }
        } else {
            // if not a leaf, it's internal, so insert that way
            InternalNode rootint = (InternalNode) root;
            this.root = rootint.insert(record, key, pointer);
        }
    }

    public void delete(int key) {
        root.delete(key);
    }

    public void writeToFile() {

    }
}
