import java.util.List;

public class LeafNode extends Node {

    private List<Integer> keys;
    private List<Integer> pointers;

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

    // displays the content of the node
    public void display() {
        System.out.println(this.keys.toString());
        System.out.println(this.pointers.toString());
    }
}
