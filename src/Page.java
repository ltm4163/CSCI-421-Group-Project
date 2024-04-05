import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Page {
    private List<Record> records;
    private int pageNumber;
    private int tableNumber;
    private boolean updated; // does this page need to be written to disk?
    private int size; 
    private int numRecords;

    public Page(int pageNumber, int tableNumber, boolean updated) {
        this.pageNumber = pageNumber;
        this.tableNumber = tableNumber;
        this.updated = updated;
        this.records = new ArrayList<>();
        this.size = Integer.BYTES; 
        this.numRecords = 0;
    }

    public void addRecord(Record record) {
        this.records.add(record);
        this.numRecords++;
        this.size += record.getSize();
        this.updated = true;
    }

    public void deleteRecord(Record record, int index) {
        this.records.remove(index);
        this.numRecords--;
        this.size -= record.getSize();
        this.updated = true;
    }

    public void shiftRecordsAndAdd(Record rec, int startingIndex) {
        this.records.add(rec);
        for (int i = this.getNumRecords(); i > startingIndex; i--) {
            this.records.set(i, records.get(i-1));
        }
        this.records.set(startingIndex, rec);
        this.numRecords++;
        this.size += rec.getSize();
        this.updated = true;
    }

    public boolean isOverfull() {
        return getSize() > Main.getPageSize();
    }

    // page + records -> binary
    public byte[] toBinary(TableSchema tableSchema) {
        ByteBuffer buffer = ByteBuffer.allocate(Main.getPageSize());
        buffer.putInt(getNumRecords()); // First 4 bytes for the number of records

        for (Record record : records) {
            byte[] recordBytes = record.toBinary(tableSchema.getattributes()); // Assuming Record has a toBinary method
            buffer.put(recordBytes);
        }

        return buffer.array();
    }

    // binary -> page + records
    public static Page fromBinary(byte[] data, int tableNumber, int pageNumber, Catalog catalog) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        TableSchema tableSchema = catalog.getTableSchema(tableNumber);
        int numRecords = buffer.getInt(); // First 4 bytes for the number of records
        AttributeSchema[] attributeSchemas = tableSchema.getattributes();

        Page page = new Page(pageNumber, tableNumber, false);

        // Populate page with records using data from file
        for (int i = 0; i < numRecords; i++) { // iterare through records
            int recordSize = 0; // used for recording size of record
            ArrayList<Object> attrValues = new ArrayList<>(tableSchema.getnumAttributes());
            byte[] bitMap = new byte[tableSchema.getnumAttributes()];
            buffer.get(bitMap, 0, tableSchema.getnumAttributes());
            ArrayList<Byte> nullBitMap = new ArrayList<>();
            for (byte b : bitMap) {
                nullBitMap.add(b);
            }

            for (int j = 0; j < tableSchema.getnumAttributes(); j++) { // iterate through attributes
                if (nullBitMap.get(j) == (byte)1) {
                    attrValues.add(null);
                    continue;
                }
                AttributeSchema attr = attributeSchemas[j];
                String attrType = attr.gettype();

                if (attrType.equalsIgnoreCase("varchar")) {
                    int sizeOfString = buffer.getInt(); //if type is varchar, read int that tells length of varchar
                    byte[] attrValueBytes = new byte[sizeOfString];
                    buffer.get(attrValueBytes, 0, sizeOfString);
                    String attrValue = new String(attrValueBytes);
                    recordSize += attrValue.length() + Integer.BYTES;
                    attrValues.add(attrValue);
                }
                else if (attrType.equalsIgnoreCase("char")) {
                    int sizeOfString = attr.getsize(); //used to tell how big string is
                    byte[] attrValueBytes = new byte[sizeOfString];
                    buffer.get(attrValueBytes, 0, sizeOfString);
                    String attrValue = new String(attrValueBytes);
                    recordSize += sizeOfString;
                    attrValues.add(attrValue);
                }
                else if (attrType.equalsIgnoreCase("integer")) {
                    int attrValue = buffer.getInt();
                    recordSize += Integer.BYTES;
                    attrValues.add(attrValue);
                }
                else if (attrType.equalsIgnoreCase("double")) {
                    double attrValue = buffer.getDouble();
                    recordSize += Double.BYTES;
                    attrValues.add(attrValue);
                }
                else if (attrType.equalsIgnoreCase("boolean")) {
                    byte attrValueByte = buffer.get();
                    boolean attrValue = (boolean)(attrValueByte == 1 ? true : false);
                    recordSize += 1;
                    attrValues.add(attrValue);
                }
            }

            Record record = new Record(attrValues, recordSize, nullBitMap);
            page.addRecord(record);
            page.size += recordSize;
        }

        return page;
    }

    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
        this.updated = true;
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
        return this.numRecords;
    }

    public void setNumRecords(int numRecords) {
        this.numRecords = numRecords;
    }

    public int getRecordCount() {
        return records.size();
    }
}
