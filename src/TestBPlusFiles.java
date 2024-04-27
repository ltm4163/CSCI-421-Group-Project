public class TestBPlusFiles {
    
    public void testWrite() {
        AttributeSchema attr = new AttributeSchema("testing", "integer", false, false, true, Integer.BYTES);
        BPlusTree tree = new BPlusTree(attr, 0);
        tree.insert(null, 0, 0);
        tree.insert(null, 3, 0);
        tree.insert(null, 2, 0);
        tree.insert(null, 4, 0);
    }
    
}
