
public interface StorageManager {
    void getRecords(int tableNumber);
    void addRecord(Catalog catalog, Record record, int tableNumber);
    Page getPage(int tableNumber, int pageNumber);
    Page findPage(int tableNumber, int pageNumber);
    Record getRecord(int tableNumber, Object primaryKey);
    void initialize();
}

// public interface Buffer {
//     void put(Page page);
//     Page get(int pageNumber);
//     void initialize(Page[] data, int size);
//     boolean isFull();
//     void writePageToHardware(Page page);
//     void writeBufferToHardware();
// }
