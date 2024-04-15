public abstract class Node {

    protected boolean isLeaf;
    protected boolean isRoot;
    protected String attrType;
    protected int tableNumber;
    protected int order; //N of tree

    public abstract int search(int key);

    public abstract Object insert(Record record, int key, int pointer);

    public abstract void delete(int key);

    public int compare(Object insertValue, Object existingValue) { //used for finding where to insert search keys
        if (attrType.equalsIgnoreCase("integer")) {
            return (int)insertValue - (int)existingValue;
        }
        return 0; //placeholder value
    }

    static class Pair<K, V> {
        private final K pageNumber;
        private final V index;

        public Pair(K first, V second) {
            this.pageNumber = first;
            this.index = second;
        }

        public K getPageNumber() {
            return pageNumber;
        }

        public V getIndex() {
            return index;
        }
    }
}
