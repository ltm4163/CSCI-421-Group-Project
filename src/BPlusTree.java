import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.ArrayList;

public class BPlusTree {

    private Node root;
    private int order;  // N value

    // default constructor

    public BPlusTree(int order, AttributeSchema attr, int tableNumber) {
        this.root = new LeafNode(order, attr, tableNumber, true);
        TableSchema tableSchema = Main.getCatalog().getTableSchema(tableNumber);
        this.root.pageNumber = tableSchema.getNumNodes();
        tableSchema.addTreeNode();
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
            root = new BPlusNode(order, true, 0);
            root.insert(record, key, pointer);
        } else {
            root.insert(record, key, pointer);
        }
    }

    public void delete(int key) {
        root.delete(key);
    }

    public void writeToFile() {
        root.writeToFile();
    }

    public static BPlusTree fromFile(int tableNumber, int order) {
        String fileName = Main.getDbDirectory() + "/indexFile/" + tableNumber + ".bin";
        byte[] data = new byte[Main.getPageSize()];

        // read in node data from file
        try (RandomAccessFile fileIn = new RandomAccessFile(fileName, "r")) {
            fileIn.read(data);
        } catch (Exception e) {
            // TODO: handle exception
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        TableSchema tableSchema = Main.getCatalog().getTableSchema(tableNumber);
        int numKeys = buffer.getInt(); // First 4 bytes for the number of keys

        AttributeSchema[] attributeSchemas = tableSchema.getattributes();
        AttributeSchema primaryKey = null;
        String attrType = primaryKey.gettype();
        for (AttributeSchema attributeSchema : attributeSchemas) { // find primary key (indexing attribute)
            if (attributeSchema.getprimarykey()) {
                primaryKey = attributeSchema;
                break;
            }
        }

        // TODO: change this to general node after internal and leaf nodes are combined
        // reconstruct root node from file
        InternalNode root = new InternalNode(order, primaryKey, tableNumber, true);
        LinkedList<Object> keys = new LinkedList<>();
        LinkedList<Node.Pair<Integer, Integer>> pointers = new LinkedList<>();

        //get first pointer pair from buffer
        int pageNumber = buffer.getInt();
        int index = buffer.getInt();
        Node.Pair<Integer, Integer> pointer = new Node.Pair<Integer,Integer>(pageNumber, index);
        pointers.add(pointer);

        // Populate root with keys and pointers using data from file
        for (int i = 0; i < numKeys; i++) {

            if (attrType.equalsIgnoreCase("varchar")) {
                int sizeOfString = buffer.getInt(); //if type is varchar, read int that tells length of varchar
                byte[] attrValueBytes = new byte[sizeOfString];
                buffer.get(attrValueBytes, 0, sizeOfString);
                String attrValue = new String(attrValueBytes);
                keys.add(attrValue);
            }
            else if (attrType.equalsIgnoreCase("char")) {
                int sizeOfString = primaryKey.getsize(); //used to tell how big string is
                byte[] attrValueBytes = new byte[sizeOfString];
                buffer.get(attrValueBytes, 0, sizeOfString);
                String attrValue = new String(attrValueBytes);
                keys.add(attrValue);
            }
            else if (attrType.equalsIgnoreCase("integer")) {
                int attrValue = buffer.getInt();
                keys.add(attrValue);
            }
            else if (attrType.equalsIgnoreCase("double")) {
                double attrValue = buffer.getDouble();
                keys.add(attrValue);
            }
            else if (attrType.equalsIgnoreCase("boolean")) {
                byte attrValueByte = buffer.get();
                boolean attrValue = (boolean)(attrValueByte == 1 ? true : false);
                keys.add(attrValue);
            }

            pageNumber = buffer.getInt();
            index = buffer.getInt();
            pointer = new Node.Pair<Integer,Integer>(pageNumber, index);
            pointers.add(pointer);
        }
        
        return null; //placeholder
    }
}
