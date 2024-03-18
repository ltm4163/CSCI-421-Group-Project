import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Catalog {
    private List<TableSchema> tables;
    private int tableCount;

    private String dbDirectory;
    private int pageSize;
    private int bufferSize;

    public Catalog(String dbDirectory, int pageSize, int bufferSize) {
        this.tables = new ArrayList<>();
        this.tableCount = 0;
        this.dbDirectory = dbDirectory;
        this.pageSize = pageSize;
        this.bufferSize = bufferSize;
    }

    public void addTable(TableSchema table) {
        if (table != null) {
            this.tables.add(table);
            this.tableCount++;
            System.out.println("Added table: " + table.getName());
        }
    }

    public void dropTable(String tableName) {
        boolean removed = this.tables.removeIf(table -> table.getName().equals(tableName));
        if (removed) {
            this.tableCount--;
            System.out.println("Table dropped: " + tableName);
        } else {
            System.err.println("Table not found: " + tableName);
        }
    }

    // TODO: Implement this
    public void addPage(Page newPage) {
        System.err.println("addPage method is dependent on system design and not directly implemented here.");
    }

    // TODO: Implement this
    public void addAttribute(String tableName, String attributeName, String type) {
        System.err.println("addAttribute method is dependent on system design and not directly implemented here.");
    }

    public void displayCatalog() {
        if (this.tables.isEmpty()) {
            System.out.println("No tables to display");
        } else {
            for (TableSchema table : this.tables) {
                System.out.println("Table: " + table.getName());
                table.displayTable();
            }
        }
    }

    public boolean tableExists(String name) {
        return this.tables.stream().anyMatch(table -> table.getName().equals(name));
    }

    public boolean findTableDisplay(String name) {
        TableSchema foundTable = this.tables.stream()
                                 .filter(table -> table.getName().equals(name))
                                 .findFirst()
                                 .orElse(null);
        if (foundTable != null) {
            foundTable.displayTable();
            return true;
        }
        return false;
    }

    public void writeCatalogToFile(String pathname) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(pathname))) {
            dos.writeInt(this.tableCount);
            for (TableSchema table : this.tables) {
                table.writeToStream(dos); 
            }
        }
    }

    public void readCatalogFromFile(String pathname) throws IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(pathname))) {
            this.tableCount = dis.readInt();
            this.tables.clear();
            for (int i = 0; i < this.tableCount; i++) {
                TableSchema table = TableSchema.readFromStream(dis); 
                this.tables.add(table);
            }
        }
    }

    public TableSchema getTableSchema(int tableNumber) {
        return this.tables.stream()
                .filter(table -> table.gettableNumber() == tableNumber)
                .findFirst()
                .orElse(null);
    }

    public int getNextPageNumber() {
        int maxPageNumber = -1;
        for (TableSchema table : this.tables) {
            int tableMaxPage = table.getMaxPageNumber();
            if (tableMaxPage > maxPageNumber) {
                maxPageNumber = tableMaxPage;
            }
        }
        return maxPageNumber + 1;
    }

    // TODO: Implement this
    public void updatePage(Page page) {
        System.err.println("updatePage method is dependent on system design and not directly implemented here.");
    }

    public int getTableCount() {
        return this.tableCount;
    }

    public List<TableSchema> getTables() {
        return this.tables;
    }

    public String getDbDirectory() {
        return this.dbDirectory;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public int getNextTableNumber() {
        return this.tables.size() + 1;
    }

    public TableSchema getTableSchemaByName(String tableName) {
        return this.tables.stream()
                .filter(table -> table.getName().equals(tableName))
                .findFirst()
                .orElse(null);
    }
}
