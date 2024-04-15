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

    public void insert(int key, int pointer) {
        root.insert(key, pointer);
    }

    public void delete(int key) {
        root.delete(key);
    }

    // display method wrapper
    public void display() {
        display(this.root, 0);
    }

    // recursive display method
    // TODO make this format the output to look like a B+ tree
    private void display(Node node, int level) {
        if(node != null) {
            if (node instanceof LeafNode) {
                System.out.println("Leaf Node: ");
                LeafNode leafNode = (LeafNode) node;
                leafNode.display();
            } else {
                System.out.println("Internal Node: ");
                InternalNode internalNode = (InternalNode) node;
                internalNode.display();
                for(Node child : internalNode.getChildren()) {
                    display(child, level+1);
                }
            }
        }
    }
}
