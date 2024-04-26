import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.ArrayList;

public class BPlusTree {

    private BPlusNode root;
    private int order;  // N value
    private static AttributeSchema attr;

    // default constructor

    public BPlusTree(AttributeSchema attr, int tableNumber) {
        this.order = (int) (Math.floor(Main.getPageSize()/(attr.getsize() + (2*Integer.BYTES)))-1);
        this.order = 4;
        System.out.println(this.order);
        this.attr = attr;
        this.root = null;
    }

//    public int search(int key) {
//        return root.search(key);
//    }

    public BPlusNode search(int key) {
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
    public void insert(Record record, Object key, int pointer) {
        System.out.println("Inserting: " + key);
        // if the B+Tree is completely empty, insert as new leaf
        if(isEmpty()) {
            root = new BPlusNode(order, true, 0, this.attr);
            root.insert(record, key, pointer, false);
        } else { root.insert(record, key, pointer, false); }
    }

    public void delete(Object key) {
        System.out.println("Deleting: " + key);
        root.delete(key, false);
        if(root.getChildren().size() == 1) {
            root = root.getChildren().get(0);
        }
    }

    public void update(Object keyToUpdate, Object key) {
        root.update(keyToUpdate, key);
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
        BPlusNode root = new BPlusNode(order, true, tableNumber, attr);
        LinkedList<Object> keys = new LinkedList<>();
        LinkedList<BPlusNode.Pair<Integer, Integer>> pointers = new LinkedList<>();

        //get first pointer pair from buffer
        int pageNumber = buffer.getInt();
        int index = buffer.getInt();
        BPlusNode.Pair<Integer, Integer> pointer = new BPlusNode.Pair<Integer,Integer>(pageNumber, index);
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
            pointer = new BPlusNode.Pair<Integer,Integer>(pageNumber, index);
            pointers.add(pointer);
        }
        
        return null; //placeholder
    }

    public void display() {
        root.display();
    }

    public int compare(Object insertValue, Object existingValue) { //used for finding where to insert search keys
        if (attr.gettype().equalsIgnoreCase("integer")) {
            return (int)insertValue - (int)existingValue;
        }
        return 0; //placeholder value
    }
}
