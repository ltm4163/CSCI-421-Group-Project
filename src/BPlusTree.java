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
        if(isEmpty()) {
            // TODO set root to new leafnode?
        } else if (root instanceof LeafNode) {
            LeafNode rootlf = (LeafNode) root;
            if(rootlf.isFull()) {
                // TODO split
            } else {
                rootlf.insert(record, key, pointer);
            }
        } else {
            // TODO search internal nodes
        }
    }

    public void delete(int key) {
        root.delete(key);
    }

    public void writeToFile() {

    }
}
