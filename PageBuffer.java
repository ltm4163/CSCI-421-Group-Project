import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public class PageBuffer {
    private final int capacity;
    private final LinkedHashMap<Integer, Page> pages;

    public PageBuffer(int capacity) {
        this.capacity = capacity;
        this.pages = new LinkedHashMap<>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Page> eldest) {
                boolean shouldRemove = size() > PageBuffer.this.capacity;
                if (shouldRemove) {
                    writePageToHardware(eldest.getValue());
                }
                return shouldRemove;
            }
        };
    }

    public void addPage(int pageNumber, Page page) {
        pages.put(pageNumber, page);
    }

    public Page getPage(int pageNumber) {
        return pages.get(pageNumber);
    }

    public boolean isPageInBuffer(int pageNumber) {
        return pages.containsKey(pageNumber);
    }

    // Adjusted to handle binary data writing
    public void writePageToHardware(Page page) {
        try {
            String fileName = "path/to/storage/" + page.getTableNumber() + "_" + page.getPageNumber() + ".bin";
            byte[] data = page.toBinary(); // Assuming this method exists in Page class
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
        if (pages.containsKey(pageNumber)) {
            pages.put(pageNumber, targetPage); // Replace the old page with the updated one
        } else {
            addPage(pageNumber, targetPage); // If the page wasn't in the buffer, add it
        }
    }
}
