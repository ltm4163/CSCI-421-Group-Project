
import java.util.Arrays;

public class TableSchema implements ITableSchema {
    
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
    
    public void displayTable(TableSchema table) {
        
        System.out.println("Table name: \n"+ table.getname());
        System.out.println("attributes:\n");
        for(int i = 0; i < table.getnumAttributes(); i++) {
            AttributeSchema attr=table.attributes[i];
            System.out.println("\t");
            attr.displayAttribute(attr);
    
        }
        System.out.println("Pages: \n"+ table.numPages);
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
        this.numPages++;
        if (this.pageLocations == null) {
            this.pageLocations = new int[1]; // Initialize if null
        } else {
            this.pageLocations = Arrays.copyOf(this.pageLocations, this.numPages);
        }
        this.pageLocations[this.numPages - 1] = newPage.getPageNumber(); 

        newPage.setTableNumber(this.tableNumber);
    }

    public int getMaxPageNumber() {
        if (this.numPages == 0) {
            return -1; // No pages
        }
        return this.pageLocations[this.numPages - 1];
    }

    public void updatePage(Page page) {
        // TODO: Implement this
        System.out.println("Update page: Placeholder implementation");
    }

    public int[] getPageLocations() {
        return this.pageLocations;
    }
    
}