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
            if (buffer.isPageInBuffer(tableNumber)) { 
                page = buffer.getPage(tableNumber); // get page from buffer
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
        StringBuilder recordString = new StringBuilder("|");
        for (AttributeSchema attr : attributeSchemas) {
            Object value = record.getAttributeValue(attr.getname()); 
            recordString.append(String.format(" %-10s |", value.toString()));
        }
        System.out.println(recordString);
    }
    
    public Page getPage(int tableNumber, int pageNumber) {
        Page page = buffer.getPage(pageNumber);
        if (page == null) {
            page = loadPageFromDisk(tableNumber, pageNumber);
        }
        return page;
    }
    
    @Override
    public void addRecord(Catalog catalog, Record record, int tableNumber) {
        TableSchema table = catalog.getTableSchema(tableNumber);
        
        // Find an appropriate page to insert the record, or create a new page if necessary
        Page targetPage = findInsertionPage(table, record);
        
        // Insert the record into the page
        targetPage.addRecord(record);
        
        // Check if the page is overfull and handle splitting if necessary
        if (targetPage.isOverfull()) {
            splitPage(targetPage);
        }
        
        // Update the catalog and buffer as necessary
        updateCatalogAndBufferAfterInsertion(catalog, table, targetPage);
    }

    private void splitPage(Page page) {
        List<Record> records = page.getRecords();

        // TODO: Change this implementation from list to arraylist
        // int midIndex = records.length / 2;
    
        // // Use List interface for declaration, ArrayList for instantiation
        // List<Record> firstHalf = new ArrayList<>(midIndex);
        // List<Record> secondHalf = new ArrayList<>(records.length - midIndex);
    
        // // Copy records to the first half and second half lists
        // for (int i = 0; i < midIndex; i++) {
        //     firstHalf.add(records[i]);
        // }
        // for (int i = midIndex; i < records.length; i++) {
        //     secondHalf.add(records[i]);
        // }
    
        // // Assuming Page has a method setRecords that accepts List<Record>
        // page.setRecords(firstHalf); // Keep first half in the original page
        // Page newPage = new Page(page.getTableNumber(), catalog.getNextPageNumber(), false); // Assuming catalog provides the next available page number
        // newPage.setRecords(secondHalf); // Move second half to the new page
    
        // catalog.updatePage(page); // Update the original page in the catalog
        // catalog.addPage(newPage); // Add the new page to the catalog
    
        // // Assuming buffer has methods updatePage and addPage that accept a Page
        // buffer.updatePage(page);
        // buffer.addPage(midIndex, newPage);
    }
    
    private void updateCatalogAndBufferAfterInsertion(Catalog catalog, TableSchema table, Page targetPage) {
        catalog.updatePage(targetPage); 
        buffer.updatePage(targetPage);
    }
    

    private Page findInsertionPage(TableSchema table, Record record) {
        for (int i = 0; i < table.getNumPages(); i++) {
            Page page = getPage(table.gettableNumber(), i);
            
            // TODO: implement finding insertion point based off primary key

        }
    
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
