public abstract class Node {

    protected boolean isLeaf;
    protected boolean isRoot;
    protected AttributeSchema attr;
    protected int tableNumber;
    protected int pageNumber; //location in index file
    protected int order; //N of tree

    public abstract int search(Object key);

    public abstract Object insert(Record record, Object key, int pointer);

    public abstract void delete(int key);

    public abstract void writeToFile();

    public int compare(Object insertValue, Object existingValue) { //used for finding where to insert search keys
        if (insertValue instanceof Integer) {
            return ((Integer) insertValue).compareTo((Integer) existingValue);
        } else if (insertValue instanceof Double) {
            return ((Double) insertValue).compareTo((Double) existingValue);
        } else if (insertValue instanceof String) {
            return ((String) insertValue).compareTo((String) existingValue);
        } else {
            // Handle other types if needed
            throw new IllegalArgumentException("Unsupported type");
        }
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
