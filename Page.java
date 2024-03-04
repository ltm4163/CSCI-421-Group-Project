import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Page {
    private List<Record> records;
    private int pageNumber;
    private int tableNumber;
    private boolean updated; // Indicates whether the page needs to be written to disk

    // Constructor initializes the page with basic information and an empty list of records.
    public Page(int pageNumber, int tableNumber, boolean updated) {
        this.pageNumber = pageNumber;
        this.tableNumber = tableNumber;
        this.updated = updated;
        this.records = new ArrayList<>();
    }

    // Adds a record to the page, marking it as updated.
    public void addRecord(Record record) {
        this.records.add(record);
        this.updated = true;
    }

    // Check if the page is overfull.
    public boolean isOverfull() {
        // TODO: check the total size of records against a maximum page size
        return false;
    }

    // Serializes the page and its records to a binary format.
    public byte[] toBinary() {
        ByteBuffer buffer = ByteBuffer.allocate(calculateBinarySize());
        buffer.putInt(records.size()); // First 4 bytes for the number of records

        for (Record record : records) {
            byte[] recordBytes = record.toBinary(); // Assuming Record has a toBinary method
            buffer.put(recordBytes);
        }

        return buffer.array();
    }

    // Deserializes a page from a binary format.
    public static Page fromBinary(byte[] data, int tableNumber, int pageNumber) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int numRecords = buffer.getInt(); // First 4 bytes for the number of records

        Page page = new Page(pageNumber, tableNumber, false);

        // TODO: Logic to deserialize each record and add it to the page

        return page;
    }

    // This is a helper method for toBinary().
    private int calculateBinarySize() {
        int size = Integer.BYTES; // Starting with 4 bytes for the number of records
        for (Record record : records) {
            size += record.toBinary().length; // Add the size of each record's binary representation
        }
        return size;
    }

    // Getters and setters
    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
        this.updated = true; // Mark as updated since the records have been modified
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public boolean hasSpaceFor(Record record) {
        // TODO: Get max records working as a constant
        final int MAX_RECORDS_PER_PAGE = 100; 
        return this.records.size() < MAX_RECORDS_PER_PAGE;
    }

    public void setTableNumber(int newTableNumber) {
        this.tableNumber = newTableNumber;
    }
}
