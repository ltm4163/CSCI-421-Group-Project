import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {
    private static Catalog catalog;
    private static PageBuffer buffer;
    private static String dbDirectory;
    private static int pageSize;
    private static int bufferSize;

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java Main <db location> <page size> <buffer size>");
            return;
        }

        dbDirectory = args[0];
        pageSize = Integer.parseInt(args[1]);
        bufferSize = Integer.parseInt(args[2]);
        
        String catalogPath = dbDirectory + "/catalog.bin";
        System.out.println("Welcome to JottQL");
        System.out.println("Looking for catalog at " + catalogPath + "...");

        catalog = new Catalog(null, 0); 
        buffer = new PageBuffer(bufferSize);
        StorageManager storageManager = new IStorageManager(catalog, pageSize);

        // Testing begin
        // StorageManagerTest.testGetPage(catalog, storageManager);
        // StorageManagerTest.testGetRecords(storageManager);
        StorageManagerTest.testInsert(catalog, storageManager);
        // Testing end

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String inputLine;
            System.out.println("\nPlease enter commands, enter <quit> to shutdown the db\n");
            while (true) {
                System.out.print("JottQL> ");
                inputLine = reader.readLine();
                if (inputLine == null || inputLine.trim().equalsIgnoreCase("<quit>")) {
                    break;
                }
                parser.parse(inputLine, catalog, buffer, dbDirectory, pageSize);
            }
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
        }

        writeBufferToHardware();
        try {
            writeCatalogToFile(catalogPath);
        } catch (IOException e) {
            System.err.println("Error saving catalog: " + e.getMessage());
        }

        System.out.println("Exiting...");
    }

    public static Catalog getCatalog() {
        return catalog;
    }

    public static PageBuffer getBuffer() {
        return buffer;
    }

    public static String getDbDirectory() {
        return dbDirectory;
    }

    public static int getPageSize() {
        return pageSize;
    }

    public static int getBufferSize() {
        return bufferSize;
    }

    public static void writeBufferToHardware() {
        // TODO: implement this
        System.out.println("writeBufferToHardware...");
    }

    public static void writeCatalogToFile(String catalogPath) throws IOException {
        // TODO: implement this
        System.out.println("writeCatalogToFile...");
    }
}
