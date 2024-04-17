import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TableSchema {
    
    private AttributeSchema[] attributes;
    private String name;
    private int tableNumber;
    private int numPages;
    private int numAttributes;
    private int[] pageLocations; //index: location; value: pageNum
    private int numNodes; //number of nodes in index tree
    private ArrayList<Integer> freeSpaces; //open locations in index file for nodes (resulting from deleted nodes)
    
    public TableSchema(int numAttributes, String name, int tableNumber, AttributeSchema[] attributes) {
        this.numAttributes = numAttributes;
        this.name = name;
        this.tableNumber=tableNumber;
        this.attributes = attributes;
        this.numPages=0;
        this.pageLocations = new int[0];
        this.numNodes = 0;
        this.freeSpaces = new ArrayList<>();
    }

    public void addTreeNode() {
        this.numNodes++;
    }

    public void deleteTreeNode() {
        this.numNodes--;
    }

    public int getNumNodes() {
        return this.numNodes;
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

    public boolean hasAttribute(String columnName) {
        return Arrays.stream(this.attributes)
                     .anyMatch(attr -> attr.getname().equals(columnName));
    }

    public List<String> getAttributeNames() {
        List<String> attributeNames = new ArrayList<>();
        for (AttributeSchema attribute : this.attributes) {
            attributeNames.add(attribute.getname());
        }

        return attributeNames;
    }

    public boolean contains(String attributeName) {
        List<String> attributeNames = getAttributeNames();
        return attributeNames.contains(attributeName);
    }

    // Gets the attribute names in the format "tableName.attributeName"; i.e. baz.id
    public List<String> getAttributeNamesWithTable() {
        List<String> attributeNames = new ArrayList<>();
        for (AttributeSchema attribute : this.attributes) {
            attributeNames.add(this.name + "." + attribute.getname());
        }

        return attributeNames;
    }

    public AttributeSchema getAttributeByName(String attrName) {
        for (AttributeSchema attribute : this.attributes) {
            if (attribute.getname().equalsIgnoreCase(attrName)) {
                return attribute;
            }
        }
        return null;
    }

    public int getAttributeIndex(String attrName) {
        AttributeSchema[] attributes = getattributes();
        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i].getname().equals(attrName)) {
                return i;
            }
        }
        return 0;
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
        PageBuffer buffer = Main.getBuffer(); 
        buffer.addPage(newPage.getPageNumber(), newPage);
        this.numPages++;
        updatePageLocations(newPage.getPageNumber());
    }

    private void updatePageLocations(int newPageNumber) {
        if (this.pageLocations == null) {
            this.pageLocations = new int[1];
        } else {
            int[] newPageLocations = new int[this.numPages];
            for (int i = 0; i < this.numPages-1; i++) {
                newPageLocations[i] = this.pageLocations[i];
                if (newPageLocations[i] >= newPageNumber) newPageLocations[i]++;
            }
            this.pageLocations = newPageLocations;
        }
        this.pageLocations[this.numPages - 1] = newPageNumber;
    }

    public void dropPage(int pageNum) {
        this.numPages--;
        
        // Find the index of the element to remove
        int indexToRemove = -1;
        for (int i = 0; i < this.numPages+1; i++) {
            if (this.pageLocations[i] == pageNum) {
                indexToRemove = i;
                break;
            }
        }

        // If the element is found, create a new array without it
        if (indexToRemove != -1) {
            int[] newArray = new int[this.numPages];
            int newIndex = 0;
            for (int i = 0; i < this.numPages+1; i++) {
                if (i != indexToRemove) {
                    newArray[newIndex] = this.pageLocations[i];
                    if (newArray[newIndex] > pageNum) {
                        newArray[newIndex]--; //decrease pageNum of pages after deleted page
                        Page page = Main.getStorageManager().getPage(tableNumber, pageNum);
                        page.setPageNumber(page.getPageNumber()-1);
                        Main.getBuffer().updatePage(page);
                    }
                    newIndex++;
                }
            }
            this.pageLocations = newArray;
        } else {
            System.out.println("Element not found in the array.");
        }
    }

    public int getMaxPageNumber() {
        if (this.numPages == 0) {
            return -1; // No pages
        }
        return this.pageLocations[this.numPages - 1];
    }

    public void updatePage(Page page) {
        PageBuffer buffer = Main.getBuffer(); 
        buffer.updatePage(page);
    }

    public int[] getPageLocations() {
        return this.pageLocations;
    }

    public String getNumRecords() {
        int totalRecords = 0;
        for (int pageLocation : this.pageLocations) {
            Page page = getPageByNumber(pageLocation);
            if (page != null) {
                totalRecords += page.getRecordCount(); 
            }
        }
        return String.valueOf(totalRecords);
    }

    public Page getPageByNumber(int pageNumber) {
        StorageManager storageManager = Main.getStorageManager();
        Page page = storageManager.getPage(this.tableNumber, pageNumber);
        return page;
    }

    public void addAttribute(AttributeSchema newAttr) {
        this.attributes = Arrays.copyOf(this.attributes, this.attributes.length + 1);
        this.attributes[this.attributes.length - 1] = newAttr;
        this.numAttributes++;
        System.out.println("Attribute " + newAttr.getname() + " added to table " + this.name);
    }

    public void dropAttribute(String attrName) {
        this.attributes = Arrays.stream(this.attributes)
                                .filter(attr -> !attr.getname().equals(attrName))
                                .toArray(AttributeSchema[]::new);
        this.numAttributes--;
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
        System.out.println("Table name: " + this.getName());
        System.out.println("Table schema:");
        AttributeSchema[] attributes = this.getattributes();
        for (AttributeSchema attr : attributes) {
            String attributeDetails = String.format("    %s:%s", attr.getname(), attr.gettype());
            if (attr.isPrimaryKey()) attributeDetails += " primarykey";
            if (attr.isUnique()) attributeDetails += " unique";
            if (attr.isNonNull()) attributeDetails += " notnull";
            System.out.println(attributeDetails);
        }
        System.out.println("Pages: " + this.getNumPages());
        System.out.println("Records: " + this.getNumRecords() + "\n");
    }

    @Override
    public String toString() {
        return this.name;
    }
}