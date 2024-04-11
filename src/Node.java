public abstract class Node {

    private boolean isLeaf;

    public abstract int search(int key);

    public abstract void insert(int key, int pointer);

    public abstract void delete(int key);
}
