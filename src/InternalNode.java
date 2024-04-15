import java.util.List;

public class InternalNode extends Node {

    private List<Integer> keys;
    private List<Node> children;

    @Override
    public int search(int key) {
        return 0;
    }

    @Override
    public void insert(int key, int pointer) {

    }

    @Override
    public void delete(int key) {

    }

    public List<Node> getChildren() {
        return this.children;
    }

    // displays the contents of the node
    public void display() {
        System.out.println(this.keys.toString());
        System.out.println(this.children.toString());
    }
}
