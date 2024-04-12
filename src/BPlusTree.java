public class BPlusTree {

    private Node root;
    private int order;  // N value

    public BPlusTree(int order) {
        this.root = new LeafNode();
        this.order = order;
    }

    public int search(int key) {
        return root.search(key);
    }

    public void insert(Record record, int key, int pointer) {
        root.insert(record, key, pointer);
    }

    public void delete(int key) {
        root.delete(key);
    }

    public void writeToFile() {

    }
}
