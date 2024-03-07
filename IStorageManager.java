import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

public class IStorageManager implements StorageManager {
    private Catalog catalog;
    private PageBuffer buffer;

    public IStorageManager(Catalog catalog, int bufferCapacity) {
        this.catalog = catalog;
        this.buffer = new PageBuffer(bufferCapacity);
    }
    

    @Override
    public void getRecords(int tableNumber) {
        TableSchema table = catalog.getTableSchema(tableNumber);
        List<Page> pages = new ArrayList<>();
        
        for (int i = 0; i < table.getNumPages(); i++) { // get all pages for table from buffer and file
            Page page;
            if (buffer.isPageInBuffer(tableNumber, i)) { 
                page = buffer.getPage(tableNumber, i); // get page from buffer
            } else {
                page = getPage(tableNumber, i); // get page from file
            }
            pages.add(page);
        }

        printAttributeNames(table.getattributes());
        
        for (Page page : pages) {
            for (Record record : page.getRecords()) {
                printRecord(record, table.getattributes());
            }
        }
    }

    private void printAttributeNames(AttributeSchema[] attributeSchemas) {
        StringBuilder header = new StringBuilder("|");
        StringBuilder separator = new StringBuilder("+");
        for (AttributeSchema attr : attributeSchemas) {
            String attrName = String.format(" %-10s |", attr.getname());
            header.append(attrName);
            separator.append("-".repeat(attrName.length() - 1)).append("+");
        }
        System.out.println(separator);
        System.out.println(header);
        System.out.println(separator);
    }
    
    private void printRecord(Record record, AttributeSchema[] attributeSchemas) {
        if (record.getdata().position() > 0) record.getdata().rewind();
        StringBuilder recordString = new StringBuilder("|");
        for (AttributeSchema attr : attributeSchemas) {
            
            // print value of each attribute in record
            if (attr.gettype().equals("varchar")) {
                int sizeOfString = record.getdata().getInt(); //if type is varchar, read int that tells length of varchar
                byte[] attrValueBytes = new byte[sizeOfString];
                record.getdata().get(attrValueBytes, 0, sizeOfString);
                String attrValue = new String(attrValueBytes);
                recordString.append(String.format(" %-10s |", attrValue));
            }
            else if (attr.gettype().equals("char")) {
                int sizeOfString = attr.getsize(); //used to tell how big string is
                byte[] attrValueBytes = new byte[sizeOfString];
                record.getdata().get(attrValueBytes, 0, sizeOfString);
                String attrValue = new String(attrValueBytes);
                recordString.append(String.format(" %-10s |", attrValue));
            }
            else if (attr.gettype().equals("integer")) {
                int attrValue = record.getdata().getInt();
                recordString.append(String.format(" %-10s |", attrValue));
            }
            else if (attr.gettype().equals("double")) {
                double attrValue = record.getdata().getDouble();
                recordString.append(String.format(" %-10s |", attrValue));
            }
            else if (attr.gettype().equals("boolean")) {
                byte attrValueByte = record.getdata().get();
                boolean attrValue = (boolean)(attrValueByte == 1 ? true : false);
                recordString.append(String.format(" %-10s |", attrValue));
            }
        }
        System.out.println(recordString);
    }
    
    public Page getPage(int tableNumber, int pageNumber) {
        Page page = buffer.getPage(tableNumber, pageNumber);
        if (page == null) {
            page = loadPageFromDisk(tableNumber, pageNumber);
        }
        return page;
    }
    
    @Override
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

                for (AttributeSchema attr : attributes) {
                    if (attr.getunique() || attr.getnotnull()) maintainConstraints = true;
                    if (indexFound && !maintainConstraints) break;
                    if (attr.getprimarykey()) {
                        int comparisonResult = compare(attr, record, existingRecord);
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
                            int comparisonResult = compare(attr, record, existingRecord);
                            if (comparisonResult == 0) {
                                // TODO: cancel insert
                                return;
                            }
                        }
                        if (attr.getnotnull()) {
                            //TODO: check if value equals agreed upon NULL placeholder
                            //  cancel insert if so
                        }
                    }
                }
            }

        }
        // Insert the record into the page
        insertPage(table, record, tableNumber, indexFound, pageIndex, recIndex);
    }

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

    private void splitPage(Page page) {
        List<Record> records = page.getRecords();

        // TODO: Change this implementation from list to arraylist
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
            firstPageSize += record.getsize();
        }
        for (int i = midIndex; i < page.getNumRecords(); i++) {
            Record record = records.get(i);
            secondHalf.add(record);
            secondPageSize += record.getsize();
        }
    
        // Assuming Page has a method setRecords that accepts List<Record>
        page.setRecords(firstHalf); // Keep first half in the original page
        page.setNumRecords(midIndex);
        page.setSize(firstPageSize);
        Page newPage = new Page(page.getTableNumber(), page.getPageNumber()+1, true);
        newPage.setRecords(secondHalf); // Move second half to the new page
        newPage.setNumRecords(page.getNumRecords() - midIndex);
        newPage.setSize(secondPageSize);
    
        catalog.getTableSchema(page.getTableNumber()).addPage(newPage);
        // catalog.updatePage(page); // Update the original page in the catalog
        // catalog.addPage(newPage); // Add the new page to the catalog

        // update pageNumbers of pages in buffer that were moved in pageLocations array
        for (int i = 0; i < catalog.getTables()[page.getTableNumber()].getNumPages(); i++) {
            Page bufferPage = buffer.getPage(page.getTableNumber(), i);
            if (bufferPage.getPageNumber() >= newPage.getPageNumber()) {
                bufferPage.setPageNumber(bufferPage.getPageNumber()+1);
            }
            buffer.updatePage(bufferPage);
        }
    
        // Assuming buffer has methods updatePage and addPage that accept a Page
        buffer.updatePage(page);
        buffer.addPage(newPage.getPageNumber(), newPage);
    }
    
    // private void updateCatalogAndBufferAfterInsertion(Catalog catalog, TableSchema table, Page targetPage) {
    //     catalog.updatePage(targetPage); 
    //     buffer.updatePage(targetPage);
    // }
    
    // Tells findInsertionPage if current location is where to insert record
    private int compare(AttributeSchema attr, Record record, Record existingRecord) {
        if (record.getdata().position() > 0) record.getdata().rewind();
        if (existingRecord.getdata().position() > 0) existingRecord.getdata().rewind();
        if (attr.gettype().equals("varchar")) {
            int sizeOfStringInsert = record.getdata().getInt(); //if type is varchar, read int that tells length of varchar
            byte[] attrValueInsertBytes = new byte[sizeOfStringInsert];
            record.getdata().get(attrValueInsertBytes, 0, sizeOfStringInsert);
            String attrValueInsert = new String(attrValueInsertBytes);

            int sizeOfStringExisting = existingRecord.getdata().getInt(); //if type is varchar, read int that tells length of varchar
            byte[] attrValueExistingBytes = new byte[sizeOfStringExisting];
            existingRecord.getdata().get(attrValueExistingBytes, 0, sizeOfStringExisting);
            String attrValueExisting = new String(attrValueExistingBytes);
            
            return attrValueInsert.compareTo(attrValueExisting);
        }
        else if (attr.gettype().equals("char")) {
            int sizeOfString = attr.getsize(); //if type is varchar, read int that tells length of varchar
            byte[] attrValueInsertBytes = new byte[sizeOfString];
            record.getdata().get(attrValueInsertBytes, 0, sizeOfString);
            String attrValueInsert = new String(attrValueInsertBytes);

            byte[] attrValueExistingBytes = new byte[sizeOfString];
            existingRecord.getdata().get(attrValueExistingBytes, 0, sizeOfString);
            String attrValueExisting = new String(attrValueExistingBytes);
            
            return attrValueInsert.compareTo(attrValueExisting);
        }
        else if (attr.gettype().equals("integer")) {
            int attrValueInsert = record.getdata().getInt();
            int attrValueExisting = existingRecord.getdata().getInt();
            return attrValueInsert-attrValueExisting;
        }
        else if (attr.gettype().equals("double")) {
            double attrValueInsert = record.getdata().getDouble();
            double attrValueExisting = existingRecord.getdata().getDouble();
            return (int)(attrValueInsert-attrValueExisting);
        }
        else if (attr.gettype().equals("boolean")) {
            byte attrValueInsertByte = record.getdata().get();
            //boolean attrValueInsert = (boolean)(attrValueInsertByte == 1 ? true : false);
            byte attrValueExistingByte = existingRecord.getdata().get();
            //boolean attrValueExisting = (boolean)(attrValueExistingByte == 1 ? true : false);
            return attrValueInsertByte-attrValueExistingByte;
        }
        return 0;
    }
    
    private Page findInsertionPage(TableSchema table, Record record) {
        
    
        Page newPage = new Page(table.gettableNumber(), table.getNextPageNumber(), false);
        table.addPage(newPage);
        return newPage;
    }
    
    @Override
    public Page findPage(int tableNumber, int pageNumber) {
        return getPage(tableNumber, pageNumber);
    }


    @Override
    public Record getRecord(int tableNumber, Object primaryKey) {
        TableSchema table = catalog.getTableSchema(tableNumber);
        for (int i = 0; i < table.getNumPages(); i++) {
            Page page = getPage(table.gettableNumber(), i); 
            for (Record record : page.getRecords()) {
                if (record.matchesPrimaryKey(primaryKey)) {
                    return record;
                }
            }
        }
        return null;
    }


    @Override
    public void initialize() {
        // TODO: Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'initialize'");
    }

    private Page loadPageFromDisk(int tableNumber, int pageNumber) {
        Page page = null;
        String fileName = Main.getDbDirectory() + "/tables/" + tableNumber + ".bin";
        try (RandomAccessFile fileIn = new RandomAccessFile(fileName, "r")) {
            byte[] data = new byte[Main.getPageSize()];
            //int address = catalog.getTables()[tableNumber].
            fileIn.seek(Integer.BYTES); // skip numPages int
            fileIn.read(data);
            page = Page.fromBinary(data, tableNumber, pageNumber, catalog);
            buffer.addPage(pageNumber, page); 
        } catch(IOException e) {
            e.printStackTrace();
        }
        return page;
    }
}
