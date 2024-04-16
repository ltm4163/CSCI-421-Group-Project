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
        for(int i = 0; i < keys.size(); i++) {
            Object searchKey = keys.get(i);
            if (searchKey != null) {
                if(compare(key, searchKey) == 0) {
                    // TODO ???
                }
                if(compare(key, searchKey) < 0) {
                    // recurse insert down to child at corresponding pointer
                }
            }
        }
        return null;
    }

    @Override
    public void delete(int key) {

    }
}
