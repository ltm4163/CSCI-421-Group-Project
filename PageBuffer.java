import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PageBuffer {
    private final int capacity;
    private final LinkedHashMap<Pair<Integer, Integer>, Page> pages;

    public PageBuffer(int capacity) {
        this.capacity = capacity;
        this.pages = new LinkedHashMap<>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Pair<Integer, Integer>, Page> eldest) {
                boolean shouldRemove = size() > PageBuffer.this.capacity;
                if (shouldRemove) {
                    writePageToHardware(eldest.getValue());
                }
                return shouldRemove;
            }
        };
    }

    public void addPage(int pageNumber, Page page) {
        Pair<Integer, Integer> key = new Pair<>(page.getTableNumber(), pageNumber);
        pages.put(key, page);
    }

    public Page getPage(int tableNumber, int pageNumber) {
        Pair<Integer, Integer> key = new Pair<>(tableNumber, pageNumber);
        return pages.get(key);
    }

    public boolean isPageInBuffer(int tableNumber, int pageNumber) {
        Pair<Integer, Integer> key = new Pair<>(tableNumber, pageNumber);
        return pages.containsKey(key);
    }

    // Adjusted to handle binary data writing
    public void writePageToHardware(Page page) {
        try {
            String fileName = "path/to/storage/" + page.getTableNumber() + "_" + page.getPageNumber() + ".bin";
            byte[] data = page.toBinary(Main.getCatalog().getTableSchema(page.getTableNumber())); // Assuming this method exists in Page class
            Files.write(Paths.get(fileName), data);
            System.out.println("Page data is saved in binary format at " + fileName);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void writeBufferToHardware() {
        pages.values().forEach(this::writePageToHardware);
    }

    public void updatePage(Page targetPage) {
        int pageNumber = targetPage.getPageNumber();
        Pair<Integer, Integer> key = new Pair<>(targetPage.getTableNumber(), pageNumber);
        if (pages.containsKey(key)) {
            pages.put(key, targetPage); // Replace the old page with the updated one
        } else {
            addPage(pageNumber, targetPage); // If the page wasn't in the buffer, add it
        }
    }

    // Define a custom Pair class to represent the key
    static class Pair<K, V> {
        private final K tableNumber;
        private final V pageNumber;

        public Pair(K first, V second) {
            this.tableNumber = first;
            this.pageNumber = second;
        }

        public K getTableNumber() {
            return tableNumber;
        }

        public V getPageNumber() {
            return pageNumber;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pair<?, ?> pair = (Pair<?, ?>) o;

            if (!tableNumber.equals(pair.tableNumber)) return false;
            return pageNumber.equals(pair.pageNumber);
        }

        @Override
        public int hashCode() {
            int result = tableNumber.hashCode();
            result = 31 * result + pageNumber.hashCode();
            return result;
        }
    }
}
