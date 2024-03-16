
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class TableSchema {
    
    private AttributeSchema[] attributes;
    private String name;
    private int tableNumber;
    private int numPages;
    private int numAttributes;
    private int[] pageLocations;
    
    public TableSchema(int numAttributes, String name, int tableNumber, AttributeSchema[] attributes) {
        this.numAttributes = numAttributes;
        this.name = name;
        this.tableNumber=tableNumber;
        this.attributes = attributes;
        this.numPages=0;
        this.pageLocations = new int[0];
    }
    
    public void setnumAttributes(int numAttributes){
        this.numAttributes=numAttributes;
    }

    public int getnumAttributes(){
        return this.numAttributes;
    }

    public void setname(String name){
        this.name=name;
    }

    public String getname(){
        return this.name;
    }

    public void settableNumber(int tableNumber){
        this.tableNumber=tableNumber;
    }

    public int gettableNumber(){
        return this.tableNumber;
    }

    public void setAttributes(AttributeSchema[] attributes){
        this.attributes=attributes;
    }

    public AttributeSchema[] getattributes(){
        return this.attributes;
    }
    
    public boolean hasPrimaryKey(TableSchema table) { 
        for(int i = 0; i < table.getnumAttributes(); i++) {
            AttributeSchema attr=table.attributes[i];
            if(attr.getprimarykey()) {
                return true;
            }
        }
        return false;
    }

    public int getNumPages() {
        return this.numPages;
    }

    public void setNumPages(int numPages) {
        this.numPages = numPages;
    }

    public int getNextPageNumber() {
        return this.numPages;
    }

    public void addPage(Page newPage) {
        PageBuffer buffer = Main.getBuffer(); // Assuming you have a way to access your PageBuffer globally or passed as a parameter
        buffer.addPage(newPage.getPageNumber(), newPage);
        this.numPages++;
        updatePageLocations(newPage.getPageNumber());
    }

    private void updatePageLocations(int newPageNumber) {
        if (this.pageLocations == null) {
            this.pageLocations = new int[1];
        } else {
            this.pageLocations = Arrays.copyOf(this.pageLocations, this.numPages);
        }
        this.pageLocations[this.numPages - 1] = newPageNumber;
    }

    public int getMaxPageNumber() {
        if (this.numPages == 0) {
            return -1; // No pages
        }
        return this.pageLocations[this.numPages - 1];
    }

    public void updatePage(Page page) {
        PageBuffer buffer = Main.getBuffer(); // Same assumption as above
        buffer.updatePage(page);
    }

    public int[] getPageLocations() {
        return this.pageLocations;
    }

    public String getNumRecords() {
        int totalRecords = 0;
        for (int pageLocation : this.pageLocations) {
            // Assuming you have a way to retrieve a Page object by its location
            // This could involve a method in your Catalog or elsewhere that can fetch a Page object given its number
            Page page = getPageByNumber(pageLocation);
            if (page != null) {
                totalRecords += page.getRecordCount(); // Assuming Page has getRecordCount()
            }
        }
        return String.valueOf(totalRecords);
    }

    public Page getPageByNumber(int pageNumber) {
        PageBuffer buffer = Main.getBuffer(); // Assuming global access or passed as a parameter
        Page page = buffer.getPage(this.tableNumber, pageNumber);
        if (page == null) {
            // Logic to load the page from disk if it's not in the buffer
            // For example:
            page = buffer.loadPageFromDisk(this.tableNumber, pageNumber);
        }
        return page;
    }

    public void addAttribute(AttributeSchema newAttr) {
        this.attributes = Arrays.copyOf(this.attributes, this.attributes.length + 1);
        this.attributes[this.attributes.length - 1] = newAttr;
        System.out.println("Attribute " + newAttr.getname() + " added to table " + this.name);
    }

    public void dropAttribute(String attrName) {
        this.attributes = Arrays.stream(this.attributes)
                                .filter(attr -> !attr.getname().equals(attrName))
                                .toArray(AttributeSchema[]::new);
        System.out.println("Attribute " + attrName + " removed from table " + this.name);
    }

    public String getName() {
        return this.name;
    }

    public void writeToStream(DataOutputStream dos) throws IOException {
        dos.writeInt(this.numAttributes);
        dos.writeUTF(this.name);
        dos.writeInt(this.tableNumber);
        dos.writeInt(this.numPages);
        for (AttributeSchema attr : this.attributes) {
            attr.writeToStream(dos);
        }
        for (int location : this.pageLocations) {
            dos.writeInt(location);
        }
    }

    public static TableSchema readFromStream(DataInputStream dis) throws IOException {
        int numAttributes = dis.readInt();
        String name = dis.readUTF();
        int tableNumber = dis.readInt();
        int numPages = dis.readInt();
        AttributeSchema[] attributes = new AttributeSchema[numAttributes];
        for (int i = 0; i < numAttributes; i++) {
            attributes[i] = AttributeSchema.readFromStream(dis);
        }
        TableSchema table = new TableSchema(numAttributes, name, tableNumber, attributes);
        table.numPages = numPages;
        table.pageLocations = new int[numPages];
        for (int i = 0; i < numPages; i++) {
            table.pageLocations[i] = dis.readInt();
        }
        return table;
    }

    public void displayTable() {
        // Display table name and number of attributes
        System.out.println("Table name: " + this.name);
        System.out.println("Number of attributes: " + this.numAttributes);
        System.out.println("Table Number: " + this.tableNumber);
        System.out.println("Number of pages: " + this.numPages);
    
        // Display attribute details
        System.out.println("Attributes:");
        for (AttributeSchema attr : this.attributes) {
            // Using displayAttribute method from AttributeSchema, assuming it prints the attribute details
            attr.displayAttribute();
        }
    
        // Optionally, display page locations if needed
        System.out.println("Page locations (Page numbers):");
        if (this.pageLocations.length > 0) {
            for (int location : this.pageLocations) {
                System.out.println(" - Page number: " + location);
            }
        } else {
            System.out.println("No pages currently associated with this table.");
        }
    
        // Displaying total number of records, assuming getNumRecords returns a string representation of the count
        System.out.println("Total records: " + this.getNumRecords());
    }
    
}