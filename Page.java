import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Page {
    private List<Record> records;
    private int pageNumber;
    private int tableNumber;
    private boolean updated; // Indicates whether the page needs to be written to disk
    private int size; // Number of bytes of data in page
    private int numRecords;

    // Constructor initializes the page with basic information and an empty list of records.
    public Page(int pageNumber, int tableNumber, boolean updated) {
        this.pageNumber = pageNumber;
        this.tableNumber = tableNumber;
        this.updated = updated;
        this.records = new ArrayList<>();
        this.size = Integer.BYTES; //accounts for numRecs int
        this.numRecords = 0;
    }

    // Adds a record to the page, marking it as updated.
    public void addRecord(Record record) {
        this.records.add(record);
        this.numRecords++;
        this.size += record.getsize();
        this.updated = true;
    }

    public void shiftRecordsAndAdd(Record rec, int startingIndex) {
        this.records.add(rec);
        for (int i = this.getNumRecords(); i > startingIndex; i--) {
            this.records.set(i, records.get(i-1));
        }
        this.records.set(startingIndex, rec);
        this.numRecords++;
        this.size += rec.getsize();
        this.updated = true;
    }

    // Check if the page is overfull.
    public boolean isOverfull() {
        return getSize() > Main.getPageSize();
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
    public static Page fromBinary(byte[] data, int tableNumber, int pageNumber, Catalog catalog) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        TableSchema tableSchema = catalog.getTableSchema(tableNumber);
        int numRecords = buffer.getInt(); // First 4 bytes for the number of records
        AttributeSchema[] attributeSchemas = tableSchema.getattributes();

        Page page = new Page(pageNumber, tableNumber, false);

        // Populate page with records using data from file
        for (int i = 0; i < numRecords; i++) { // iterare through records
            int recordOffset = 0; // used for writing to record byte array
            byte[] recordData = new byte[Main.getPageSize()];

            for (int j = 0; j < tableSchema.getnumAttributes(); j++) { // iterate through attributes
                AttributeSchema attr = attributeSchemas[j];
                String attrType = attr.gettype();
                int sizeToRead = attr.getsize(); // used to know how many bytes to read for current attribute

                if (attrType.equals("varchar")) { //if type is varchar, read int that tells length of varchar
                    sizeToRead = buffer.getInt();
                    byte[] intBytes = ByteBuffer.allocate(Integer.BYTES).putInt(sizeToRead).array();
                    System.arraycopy(intBytes, 0, recordData, recordOffset, intBytes.length); // store int in record data
                    recordOffset += Integer.BYTES;
                }

                buffer.get(recordData, recordOffset, sizeToRead); // write attribute value to record data
                recordOffset += sizeToRead;
            }

            ByteBuffer recDataBuffer = ByteBuffer.allocate(recordOffset);
            recDataBuffer.put(recordData, 0, recordOffset);
            Record record = new Record(recDataBuffer, recordOffset);
            page.addRecord(record);
            page.size += recordOffset;
        }

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

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
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

    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setTableNumber(int newTableNumber) {
        this.tableNumber = newTableNumber;
    }

    public int getNumRecords() {
        return numRecords;
    }

    public void setNumRecords(int numRecords) {
        this.numRecords = numRecords;
    }
}
