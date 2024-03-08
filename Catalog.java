import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

public class Catalog implements ICatalog{
    private TableSchema[] tables;
    int tableCount;
    public Catalog(TableSchema[]tables, int tablecount)
    {
        this.tables=tables;
        this.tableCount=tablecount;
    }
        
    public void addTable(Catalog c, TableSchema table){
        if(c.tables != null) {
            TableSchema[]newTables=Arrays.copyOf(c.tables, c.tables.length + 1 );
            if (newTables != null) {
                newTables[c.tableCount] = table;
                c.tables=newTables;
                c.tableCount++;
                System.out.println("added table: \n"+ table.getname());    
            }
            else {
                System.err.print("Memory allocation failed\n"); 
            }
        }   
        else 
        { 
            c.tables = new TableSchema[1];
            c.tables[0] = table;
            c.tableCount++;
        }
    }
    public void dropTable(Catalog c, String name){
        int tableIndex = -1;

        for(int i = 0; i < c.tableCount; i++) {
            if(c.tables[i].getname().equals(name)) {
                tableIndex = i;
                break;
            }
        }

        if(tableIndex != -1) {
            for(int i = tableIndex; i < c.tableCount - 1; i++) {
                c.tables[i] = c.tables[i+1];
            }

            c.tableCount--;

            TableSchema[]newTables = c.tables;

            if(newTables != null || c.tableCount == 0) {
                c.tables = newTables;
            } 
            else {
                System.err.print("Table not found in catalog\n "+name); 
            }
        }
        }

    public void addPage(Page newPage){
        //How do we deal with this one
    }

    public void addAttribute(Catalog c, String name, String type){
        // Add more constraints as needed
    } 

    public void displayCatalog(Catalog c){
        for(int i = 0; i < c.tableCount; i++) {
            TableSchema table=c.tables[i];
            table.displayTable(table);
        }
        
        if(c.tableCount == 0) {
            System.out.println("no tables to display\n");  // TODO: Do I need to include table name?
        }
    }

    public int tableExists(Catalog c, String name){
        for(int i = 0; i < c.tableCount; i++) {
            if(c.tables[i].getname().equals(name)) {
                return 1;
            }
        } 
        return 0;
    }

    public int findTableDisplay(Catalog c, String name){
        for(int i = 0; i < c.tableCount; i++) {
            if(c.tables[i].getname().equals(name)) {
                tables[i].displayTable(c.tables[i]);
                return 1;
            }
        } 
        return 0;
    }

/// @brief Writes the contents of the provided Catalog pointer to the provided pathname
/// @param c The catalog with data to write (everything must be initialized/malloc'd)
/// @param pathname The path of the .bin file to write to (if it does not exist, it will be created)
    public void writeCatalogToFile(Catalog c, String pathname) throws IOException{
        try {
            FileOutputStream fos=new FileOutputStream(pathname);
            ObjectOutputStream oos=new ObjectOutputStream(fos);
            oos.writeObject(c);
            System.out.println("c");
            oos.writeObject(c.tables);
            System.out.println("tables");
            for(int i=0; i < c.tables.length; i++){
                oos.writeObject(c.tables[i].getattributes());
                System.out.println("attr");
            }
        } 
        catch(NotSerializableException e) {
            System.out.println("Catalog can't currently be written to file like this");
        }
        catch(IOException ioe){
            System.out.println("An error has occurred while writing this file: "+ioe.getMessage());
        }
    }

/// @brief Reads the contents in the provided pathname to the provided Catalog pointer
/// @param c The catalog to receive the data (everything must be initialized/malloc'd)
/// @param pathname The path of the file
    public void readCatalogFromFile(Catalog c, String pathname) throws IOException{
        try{
            FileInputStream fis=new FileInputStream(pathname);
            ObjectInputStream ois=new ObjectInputStream(fis);
            Catalog cat=(Catalog) ois.readObject();
            TableSchema table=(TableSchema) ois.readObject();
            for(int i = 0; i < c.tableCount; i++){
                c.addTable(cat, table);
            }
            for(int j = 0; j < c.tableCount; j++){
                TableSchema tableSchema=c.tables[j];
                tableSchema.setAttributes((AttributeSchema[]) ois.readObject());
            }
            
        }
        catch(IOException  | ClassNotFoundException e){
            System.out.println("An error has occurred while writing this file: "+e.getMessage());
        }
    }




    public TableSchema getTableSchema(int tableNumber) {
        if (tableNumber >= 0 && tableNumber < this.tableCount) {
            return this.tables[tableNumber];
        } else {
            System.err.println("Invalid table number: " + tableNumber);
            return null; // Should we throw an exception here??
        }
    }
    




    public int getNextPageNumber() {
        int maxPageNumber = -1;
        for (TableSchema table : this.tables) {
            int tableMaxPage = table.getMaxPageNumber();
            if (tableMaxPage > maxPageNumber) {
                maxPageNumber = tableMaxPage;
            }
        }
        return maxPageNumber + 1; // the next available global page number
    }
    




    public void updatePage(Page page) {
        if (page != null && page.getTableNumber() >= 0 && page.getTableNumber() < this.tableCount) {
            TableSchema table = this.tables[page.getTableNumber()];
            table.updatePage(page); // You would need to implement updatePage in TableSchema
        } else {
            System.err.println("Page or table is invalid.");
        }
    }

    public int getTableCount() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTableCount'");
    }

    public TableSchema[] getTables() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTables'");
    }
    
}
