import java.util.List;

public class InternalNode extends Node {

    private List<Object> keys;
    private List<Node> children;
    private List<Pair<Integer, Integer>> pointers; //position of children in file

    @Override
    public int search(int key) {
        return 0;
    }

    @Override
    public Object insert(Record record, int key, int pointer) {
        return null; //placeholder
    }

    @Override
    public void delete(int key) {

    }
}
