import java.lang.*;

public class parser {
    private static void writeBufferToHardware(Object buffer) {
        // Implement the writeBufferToHardware method
    }

    private static void writeCatalogToFile(Catalog catalog, String dbpath) {
        // Implement the writeCatalogToFile method
    }

    private static void handleCreateCommand(String inputLine) {
        // Implement the handleCreateCommand method
    }

    private static void handleDropCommand(String inputLine) {
        // Implement the handleDropCommand method
    }

    private static void handleAlterCommand(String inputLine) {
        // Implement the handleAlterCommand method
    }

    private static void handleInsertCommand(String inputLine) {
        // Implement the handleInsertCommand method
    }

    private static void displaySchema(Catalog catalog) {
        // Implement the displaySchema method
    }

    private static boolean findTableDisplay(Catalog catalog, String tableName) {
        // Implement the findTableDisplay method
        return false;
    }

    private static void handleSelectCommand(String inputLine) {
        // Implement the handleSelectCommand method
    }

    int parse(String inputLine, String dbpath) {
        Catalog catalog = getCatalog();
        String[] tokens = inputLine.split("\\s+");
        String command =  inputTok[0];
        String next;

        if (!inputLine.contains(";") && !inputLine.equals("<quit>")) {
            System.out.println("Expected ';'");
            return 0;
        }

        switch (command) {
            case "<quit>":
                System.out.println("\nSafely shutting down the database...");
                System.out.println("Purging page buffer...");

                writeBufferToHardware(getBuffer());
                writeCatalogToFile(catalog, dbpath);

                System.out.println("Exiting the database...\n");
                return 1; // 1 = TRUE = EXIT CLI loop in main()

            case "create":
                handleCreateCommand(inputLine);
                break;

            case "drop":
                handleDropCommand(inputLine);
                break;

            case "alter":
                // This may not be needed for this phase???
                handleAlterCommand(inputLine);
                break;

            case "insert":
                handleInsertCommand(inputLine);
                break;

            case "display":
                if (tokens.length > 1) {
                    next = tokens[1];
                    if (next.equals("schema")) {
                        displaySchema(catalog);
                        System.out.println("SUCCESS\n");
                        return 0;
                    } else if (next.equals("info") && tokens.length > 2) {
                        String tableName = tokens[2].replace(";", "");
                        if (!findTableDisplay(catalog, tableName)) {
                            System.out.println("no such table " + tableName);
                            System.out.println("ERROR\n");
                        } else {
                            System.out.println("SUCCESS\n");
                        }
                        return 0;
                    }
                }
                return 0;

            case "select":
                handleSelectCommand(inputLine);
                break;

            default:
                System.out.println("Unknown command");
        }




    }
}
