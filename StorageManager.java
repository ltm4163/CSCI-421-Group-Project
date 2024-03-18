import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

public class StorageManager {
    private Catalog catalog;
    private PageBuffer buffer;

    public StorageManager(Catalog catalog, PageBuffer buffer) {
        this.catalog = catalog;
        this.buffer = buffer;
    }
    
    // Returns an ArrayList of tuples (tuples are of type ArrayList<Object>)
    public ArrayList<ArrayList<Object>> getRecords(int tableNumber) {
        TableSchema table = catalog.getTableSchema(tableNumber);
        List<Page> pages = new ArrayList<>();
        ArrayList<ArrayList<Object>> tuples = new ArrayList<>();

        for (int i = 0; i < table.getNumPages(); i++) { // get all pages for table from buffer and file
            Page page;
            if (buffer.isPageInBuffer(tableNumber, i)) { 
                page = buffer.getPage(tableNumber, i); // get page from buffer
            } else {
                page = getPage(tableNumber, i); // get page from file
            }
            pages.add(page);
        }

        for (Page page : pages) {
            for (Record record : page.getRecords()) {
                tuples.add(record.getData());
            }
        }
        return tuples;
    }
    
    // Looks for page in buffer, retrieves from file if not in buffer
    public Page getPage(int tableNumber, int pageNumber) {
        Page page = buffer.getPage(tableNumber, pageNumber);
        if (page == null) {
            page = loadPageFromDisk(tableNumber, pageNumber);
        }
        return page;
    }
    
    public void addRecord(Catalog catalog, Record record, int tableNumber) {
        TableSchema table = catalog.getTableSchema(tableNumber);
        
        // Find an appropriate page to insert the record, or create a new page if necessary
        AttributeSchema[] attributes = table.getattributes();
        boolean maintainConstraints = false; // if an attribute has unique or notNull, search entire table
        boolean indexFound = false; // flag for if insert location found
        
        // used for location to insert record in table (given placeholder values so it compiles)
        int pageIndex = -1;
        int recIndex = -1;

        for (int i = 0; i < table.getNumPages(); i++) { //iterate through all pages to find insert location
            if (indexFound && !maintainConstraints) break;
            Page page = getPage(table.gettableNumber(), i);
            List<Record> existingRecords = page.getRecords();

            for (int j = 0; j < page.getNumRecords(); j++) { //iterates through all records in page
                if (indexFound && !maintainConstraints) break;
                Record existingRecord = existingRecords.get(j);

                int tupleIndex = 0; // index of current attribute in tuple
                for (AttributeSchema attr : attributes) {
                    if (attr.getunique() || attr.getnotnull()) maintainConstraints = true;
                    if (indexFound && !maintainConstraints) break;
                    if (attr.getprimarykey()) {
                        int comparisonResult = compare(attr, record, existingRecord, tupleIndex);
                        if (comparisonResult == 0) {
                            // TODO: cancel insert
                            return;
                        }
                        else if (comparisonResult < 0) {
                            indexFound = true;
                            pageIndex = i;
                            recIndex = j;
                        }
                    }
                    else {
                        if (attr.getunique()) {
                            int comparisonResult = compare(attr, record, existingRecord, tupleIndex);
                            if (comparisonResult == 0) {
                                // TODO: cancel insert
                                return;
                            }
                        }
                        if (attr.getnotnull()) {
                            // TODO: check if value equals agreed upon NULL placeholder
                            //  cancel insert if so
                        }
                    }
                    tupleIndex++; // move to next attribute in tuple
                }
            }

        }
        // Insert the record into the page
        insertPage(table, record, tableNumber, indexFound, pageIndex, recIndex);
    }

    // Insert record into page
    private void insertPage(TableSchema table, Record record, int tableNumber, boolean indexFound, int pageIndex, int recIndex) {
        Page targetPage;
        if (table.getNumPages() == 0) {
            targetPage = new Page(0, tableNumber, true);
            targetPage.addRecord(record);
            table.addPage(targetPage);
            buffer.addPage(targetPage.getPageNumber(), targetPage);
        }
        else if (indexFound) {
            targetPage = getPage(tableNumber, pageIndex);
            targetPage.shiftRecordsAndAdd(record, recIndex); // adds record and shifts subsequent records down by 1
        }
        else {
            pageIndex = table.getNumPages()-1;
            targetPage = getPage(tableNumber, pageIndex);
            targetPage.addRecord(record);
        }
        
        // Check if the page is overfull and handle splitting if necessary
        if (targetPage.isOverfull()) {
            splitPage(targetPage);
        }
        else buffer.updatePage(targetPage);
        
        // Update the catalog and buffer as necessary
        //updateCatalogAndBufferAfterInsertion(catalog, table, targetPage);
    }

    // Split page into two
    private void splitPage(Page page) {
        List<Record> records = page.getRecords();

        // TODO: Change this implementation from list to arraylist (dont think this is necessary)
        int midIndex = page.getNumRecords() / 2;
    
        // Use List interface for declaration, ArrayList for instantiation
        List<Record> firstHalf = new ArrayList<>(midIndex);
        List<Record> secondHalf = new ArrayList<>(page.getNumRecords() - midIndex);
    
        // Copy records to the first half and second half lists
        int firstPageSize = 0;
        int secondPageSize = 0;
        for (int i = 0; i < midIndex; i++) {
            Record record = records.get(i);
            firstHalf.add(record);
            firstPageSize += record.getSize();
        }
        for (int i = midIndex; i < page.getNumRecords(); i++) {
            Record record = records.get(i);
            secondHalf.add(record);
            secondPageSize += record.getSize();
        }
    
        // Assuming Page has a method setRecords that accepts List<Record>
        Page newPage = new Page(page.getPageNumber()+1, page.getTableNumber(), true);
        newPage.setRecords(secondHalf); // Move second half to the new page
        newPage.setNumRecords(page.getNumRecords() - midIndex);
        newPage.setSize(secondPageSize);
        page.setRecords(firstHalf); // Keep first half in the original page
        page.setNumRecords(midIndex);
        page.setSize(firstPageSize);
    
        // catalog.updatePage(page); // Update the original page in the catalog
        // catalog.addPage(newPage); // Add the new page to the catalog

        // update pageNumbers of pages in buffer that were moved in pageLocations array
        for (int i = 0; i < catalog.getTableSchema(page.getTableNumber()).getNumPages(); i++) {
            Page bufferPage = buffer.getPage(page.getTableNumber(), i);
            int pageNum = bufferPage.getPageNumber();
            if (pageNum >= newPage.getPageNumber()) {
                bufferPage.setPageNumber(pageNum+1);
                buffer.updatePage(bufferPage);
            }
        }
        catalog.getTableSchema(page.getTableNumber()).addPage(newPage);
    
        // Assuming buffer has methods updatePage and addPage that accept a Page
        buffer.updatePage(page);
        buffer.addPage(newPage.getPageNumber(), newPage);
    }
    
    // Tells findInsertionPage if current location is where to insert record
    private int compare(AttributeSchema attr, Record record, Record existingRecord, int tupleIndex) {
        if (attr.gettype().equalsIgnoreCase("varchar")) {
            String attrValueInsert = (String)record.getData().get(tupleIndex);
            String attrValueExisting = (String)existingRecord.getData().get(tupleIndex);
            return attrValueInsert.compareTo(attrValueExisting);
        }
        else if (attr.gettype().equalsIgnoreCase("char")) {
            String attrValueInsert = (String)record.getData().get(tupleIndex);
            String attrValueExisting = (String)existingRecord.getData().get(tupleIndex);
            return attrValueInsert.compareTo(attrValueExisting);
        }
        else if (attr.gettype().equalsIgnoreCase("integer")) {
            int attrValueInsert = (int)record.getData().get(tupleIndex);
            int attrValueExisting = (int)existingRecord.getData().get(tupleIndex);
            return attrValueInsert-attrValueExisting;
        }
        else if (attr.gettype().equalsIgnoreCase("double")) {
            double attrValueInsert = (double)record.getData().get(tupleIndex);
            double attrValueExisting = (double)existingRecord.getData().get(tupleIndex);
            return (int)(attrValueInsert-attrValueExisting);
        }
        else if (attr.gettype().equalsIgnoreCase("boolean")) {
            boolean attrValueInsert = (boolean)record.getData().get(tupleIndex);
            boolean attrValueExisting = (boolean)existingRecord.getData().get(tupleIndex);
            byte attrValueInsertByte = (byte)(attrValueInsert ? 1 : 0);
            byte attrValueExistingByte = (byte)(attrValueExisting ? 1 : 0);
            return attrValueInsertByte-attrValueExistingByte;
        }
        return 0;
    }
    
    public Page findPage(int tableNumber, int pageNumber) {
        return getPage(tableNumber, pageNumber);
    }

    public Record getRecord(int tableNumber, Object primaryKey) {
        TableSchema table = catalog.getTableSchema(tableNumber);
        for (int i = 0; i < table.getNumPages(); i++) {
            Page page = getPage(table.gettableNumber(), i); 
            for (Record record : page.getRecords()) {
                if (record.matchesPrimaryKey(primaryKey, table)) {
                    return record;
                }
            }
        }
        return null;
    }

    public void initialize() {
        // TODO: Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'initialize'");
    }

    // Retrieve page from file
    private Page loadPageFromDisk(int tableNumber, int pageNumber) {
        Page page = null;
        String fileName = Main.getDbDirectory() + "/tables/" + tableNumber + ".bin";
        TableSchema tableSchema = catalog.getTableSchema(tableNumber);
        try (RandomAccessFile fileIn = new RandomAccessFile(fileName, "r")) {
            byte[] data = new byte[Main.getPageSize()];
            int index = -1;
            int[] pageLocations = tableSchema.getPageLocations();
            for (int i = 0; i < tableSchema.getNumPages(); i++) { // find location of page in file
                if (pageLocations[i] == pageNumber) {
                    index = i;
                    break;
                }
            }
            if (index<0) {
                System.err.println("Can't read page: No pages in table");
                return null;
            }
            int address = Integer.BYTES + (index*Main.getPageSize()); // skip numPages int, seek to page location in file
            fileIn.seek(address);
            fileIn.read(data);
            page = Page.fromBinary(data, tableNumber, pageNumber, catalog);
            buffer.addPage(pageNumber, page); 
        } catch(IOException e) {
            e.printStackTrace();
        }
        return page;
    }
}