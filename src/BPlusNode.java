import java.util.ArrayList;
import java.util.List;

public class BPlusNode {

    private int order;  // N value
    private List<String> values;
    private List<List<BPlusNode>> keys;
    private BPlusNode nextKey;
    private BPlusNode parent;
    private boolean isLeaf;

    public BPlusNode(int order) {
        this.order = order;
        this.values = new ArrayList<>();
        this.keys = new ArrayList<>();
        this.nextKey = null;
        this.parent = null;
        this.isLeaf = false;
    }


}
