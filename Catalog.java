import java.io.*;
import java.util.Arrays;

public class Catalog {

    private TableSchema[] tables;
    private int tableCount;

    // Initialization Functions:

    public Catalog() {
        tables = new TableSchema[0];
        tableCount = 0;
    }

    // Insertion Functions:

    public void addTable(TableSchema table) {
        tables = Arrays.copyOf(tables, tableCount + 1);
        tables[tableCount] = table;
        tableCount++;
        System.out.println("added table: " + table.getName());
    }

    public void dropTable(String name) {
        int tableIndex = -1;

        for (int i = 0; i < tableCount; i++) {
            if (tables[i].getName().equals(name)) {
                tableIndex = i;
                break;
            }
        }

        if (tableIndex != -1) {
            System.arraycopy(tables, tableIndex + 1, tables, tableIndex, tableCount - tableIndex - 1);
            tableCount--;

            tables = Arrays.copyOf(tables, tableCount);
        } else {
            System.err.println("Table '" + name + "' not found in catalog");
        }
    }

    public void addPage() {
        // How do we deal with this one?
    }

    public void addAttribute(String name, String type) {
        // Add more constraints as needed
    }

    public void displayCatalog() {
        for (TableSchema table : tables) {
            table.displayTable();
        }

        if (tableCount == 0) {
            System.out.println("no tables to display");
        }
    }

    public boolean tableExists(String name) {
        for (TableSchema table : tables) {
            if (table.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean findTableDisplay(String name) {
        for (TableSchema table : tables) {
            if (table.getName().equals(name)) {
                table.displayTable();
                return true;
            }
        }
        return false;
    }

    public void writeCatalogToFile(String pathname) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(pathname))) {
            oos.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Catalog readCatalogFromFile(String pathname) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(pathname))) {
            return (Catalog) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
