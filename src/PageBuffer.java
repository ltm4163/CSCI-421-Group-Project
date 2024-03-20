import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;
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

    public void writePageToHardware(Page page) {
        try {
            String fileName = Main.getDbDirectory() + "/tables/" + page.getTableNumber() + ".bin";
            TableSchema tableSchema = Main.getCatalog().getTableSchema(page.getTableNumber());
            byte[] data = page.toBinary(tableSchema); 
            RandomAccessFile fileOut = new RandomAccessFile(fileName, "rw");
            int index = -1; // placeholder value for compiling
            int[] pageLocations = tableSchema.getPageLocations();
            for (int i = 0; i < tableSchema.getNumPages(); i++) { // find location of page in file
                if (pageLocations[i] == page.getPageNumber()) {
                    index = i;
                    break;
                }
            }
            if (index<0) {
                System.out.println("Can't write page: No pages in table");
                return;
            }
            int address = Integer.BYTES + (index*Main.getPageSize()); // skip numPages, seek to page location in file
            fileOut.seek(address);
            fileOut.write(data);
            System.out.println("Page data is saved in binary format at " + fileName);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void writeBufferToHardware() {
        for(Pair<Integer, Integer> key : this.pages.keySet()) {
            this.writePageToHardware(this.pages.get(key));
        }
    }

    public void updatePage(Page targetPage) {
        int pageNumber = targetPage.getPageNumber();
        Pair<Integer, Integer> key = new Pair<>(targetPage.getTableNumber(), pageNumber);
        if (pages.containsKey(key)) {
            pages.put(key, targetPage); // replace the old page with the updated one
        } else {
            addPage(pageNumber, targetPage); // else add page
        }
    }

    // The key is a pair of tableNumber and pageNumber
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

    private String getFileNameForPage(int tableNumber, int pageNumber) {
        return Main.getDbDirectory() + "/tables/" + tableNumber + "/" + pageNumber + ".bin";
    }
}
