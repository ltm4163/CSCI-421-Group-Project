import java.util.ArrayList;

public interface StorageManager {
    ArrayList<ArrayList<Object>> getRecords(int tableNumber); // Returns an ArrayList of tuples (tuples are of type ArrayList<Object>)
    void addRecord(Catalog catalog, Record record, int tableNumber);
    Page getPage(int tableNumber, int pageNumber);
    Page findPage(int tableNumber, int pageNumber);
    Record getRecord(int tableNumber, Object primaryKey);
    void initialize();
}
