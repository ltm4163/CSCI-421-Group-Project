import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;

public class Main {
    private static Catalog catalog;
    private static PageBuffer buffer;
    private static StorageManager storageManager;
    private static String dbDirectory;
    private static int pageSize;
    private static int bufferSize;

    private static boolean indexing;
    private static ArrayList<BPlusTree> bPlusTrees;

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: java Main <db location> <page size> <buffer size> <indexing>");
            return;
        }

        dbDirectory = args[0].endsWith("/") ? args[0] : args[0] + "/";
        pageSize = Integer.parseInt(args[1]);
        bufferSize = Integer.parseInt(args[2]);
        indexing = Boolean.parseBoolean(args[3]);

        new File(dbDirectory + "tables").mkdirs();

        String catalogPath = dbDirectory + "catalog.bin";
        System.out.println("Welcome to JottQL");
        System.out.println("Looking for catalog at " + catalogPath + "...");

        catalog = new Catalog(dbDirectory, pageSize, bufferSize);
        buffer = new PageBuffer(bufferSize);
        storageManager = new StorageManager(catalog, buffer);
        bPlusTrees = new ArrayList<>();

        // Load/init the catalog
        try {
            if (new File(catalogPath).exists()) {
                catalog.readCatalogFromFile(catalogPath);
                System.out.println("Catalog loaded successfully.");
            } else {
                System.out.println("No existing catalog found. A new one will be created.");
            }
        } catch (IOException e) {
            System.err.println("Failed to load catalog: " + e.getMessage());
            return;
        }

        // Load tree roots from file
        if (indexing) {
            try {
                if (new File(dbDirectory + "/indexFiles").exists()) {
                    for (TableSchema tableSchema : catalog.getTables()) {
                        BPlusTree tree = BPlusTree.fromFile(tableSchema.gettableNumber());
                        bPlusTrees.add(tableSchema.gettableNumber(), tree);
                    }
                    System.out.println("BPlus trees loaded successfully.");
                }
            } catch (Exception e) {
                System.err.println("Failed to load bplustree");
                return;
            }
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            StringBuilder commandBuilder = new StringBuilder();
            String inputLine;
            System.out.println("\nPlease enter commands, enter <quit> to shutdown the db\n");
            while (true) {
                System.out.print("JottQL> ");
                inputLine = reader.readLine();
                if (inputLine == null || inputLine.trim().equalsIgnoreCase("quit")) {
                    if (commandBuilder.length() > 0) {
                        parser.parse(commandBuilder.toString(), catalog, buffer, dbDirectory, pageSize, storageManager);
                        commandBuilder.setLength(0);
                    }
                    break;
                }
                commandBuilder.append(" ").append(inputLine.trim());

                if (inputLine.trim().endsWith(";")) {
                    parser.parse(commandBuilder.toString().trim(), catalog, buffer, dbDirectory, pageSize, storageManager);
                    commandBuilder.setLength(0);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
        }

        // Write buffer and catalog to disk before exiting
        buffer.writeBufferToHardware();
        try {
            catalog.writeCatalogToFile(catalogPath);
        } catch (IOException e) {
            System.err.println("Error saving catalog: " + e.getMessage());
        }
        for (BPlusTree tree : bPlusTrees) {
            tree.writeToFile();
        }

        System.out.println("Exiting...");
    }

    public static Catalog getCatalog() {
        return catalog;
    }

    public static PageBuffer getBuffer() {
        return buffer;
    }

    public static StorageManager getStorageManager() {
        return storageManager;
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

    public static boolean getIndexing() {
        return indexing;
    }

    public static ArrayList<BPlusTree> getTrees() {
        return bPlusTrees;
    }

    public static void writeBufferToHardware() {
        buffer.writeBufferToHardware();
    }

    public static void writeCatalogToFile(String catalogPath) throws IOException {
        catalog.writeCatalogToFile(catalogPath);
    }
}
