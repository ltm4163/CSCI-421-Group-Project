import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.io.RandomAccessFile;
public class TestPageBuffer {
    public static void testaddPage(){
        PageBuffer buffer = new PageBuffer(3);
        Page page1 = new Page(1, 1, true);
        Page page2 = new Page(2, 2, true);
    
        buffer.addPage(1, page1);
        buffer.addPage(2, page2);
        boolean testpage1 = (page1.equals(buffer.getPage(1,1)));
        System.out.println("testpage1: " + testpage1);
        boolean testpage2 = (page2.equals(buffer.getPage(2,2)));
        System.out.println("testpage1: " + testpage2);
    }
    public void testGetPage() {
        PageBuffer buffer = new PageBuffer(3);
        Page page1 = new Page(1, 1, true);

        buffer.addPage(1, page1);

        boolean testpage1 = (page1.equals(buffer.getPage(1,1)));
        System.out.println("testpage1: " + testpage1);

    }
    public void testIsPageInBuffer() {
        PageBuffer buffer = new PageBuffer(3);
        Page page1 = new Page(1, 1, true);

        buffer.addPage(1, page1);

        boolean page1in=(buffer.isPageInBuffer(1,1));
        boolean page2in=(buffer.isPageInBuffer(2,2));
        System.out.println("Page 1 is in buffer: "+page1in);
        System.out.println("Page 2 is in buffer: "+page2in);
    }
    public void testUpdatePage() {
        PageBuffer buffer = new PageBuffer(3);
        Page page1 = new Page(1, 1, true);
        Page updatedPage1 = new Page(1, 2, true);
        Page page2 = new Page(2, 2, false);

        buffer.addPage(1, page1);
        buffer.addPage(2, page2);

        buffer.updatePage(updatedPage1);

        boolean updatedpg=(updatedPage1.equals(buffer.getPage(2, 1)));
        if(updatedpg==true){
            System.out.println("Page 1 update is: "+updatedpg);
        }
        boolean sameornot=(page2.equals(buffer.getPage(2,2)));
        System.out.println("Page 2 is the same: "+sameornot);
    }
    public static void testWritePageToHardware() {
        PageBuffer buffer = new PageBuffer(3);
        Page mockPage = new Page(1, 1, true);
        buffer.addPage(1, mockPage);
        buffer.writePageToHardware(mockPage);
        System.out.println(mockPage + " written to hardware");
        /**
        try {
            Page mockPage = new Page(1, 1, true);
            String fileName = Main.getDbDirectory() + "/tables/" + mockPage.getTableNumber() + ".bin";
            TableSchema tableSchema = Main.getCatalog().getTableSchema(mockPage.getTableNumber());
            byte[] data = mockPage.toBinary(tableSchema); // Assuming this method exists in Page class
            RandomAccessFile fileOut = new RandomAccessFile(fileName, "rw");
            int index = -1; // placeholder value for compiling
            int[] pageLocations = tableSchema.getPageLocations();
            for (int i = 0; i < tableSchema.getNumPages(); i++) { // find location of page in file
                if (pageLocations[i] == mockPage.getPageNumber()) {
                    index = i;
                    break;
                }
            }
            if (index<0) {
                //throw new Exception("No pages in table");
                System.out.println("No pages in table");
                return;
            }
            int address = Integer.BYTES + (index*Main.getPageSize()); // skip numPages int, seek to page location in file
            fileOut.seek(address);
            fileOut.write(data);
            System.out.println("Page data is saved in binary format at " + fileName);
        } catch(IOException e) {
            e.printStackTrace();
        }
        */
    }
    public void testWriteBufferToHardware() throws IOException {
        // Create a mock PageBuffer object
        PageBuffer buffer = new PageBuffer(4);

        // Create mock Page objects
        Page mockPage1 = new Page(1, 1, true);
        Page mockPage2 = new Page(2, 2, false);
        Page mockPage3 = new Page(3, 3, false);

        // Add mock pages to the buffer
        buffer.addPage(1, mockPage1);
        buffer.addPage(2, mockPage2);
        buffer.addPage(3, mockPage3);
        buffer.writeBufferToHardware();
        System.out.println("All finished writing buffer to hardware");
        /**

        // Create a ByteArrayOutputStream to capture the data written to the file
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Mock writePageToHardware method to capture the data written to the ByteArrayOutputStream
        buffer.setWritePageToHardware(page -> {
            try {
                byte[] data = page.toBinary(Main.getCatalog().getTableSchema(page.getTableNumber())); // Assuming this method exists in Page class
                outputStream.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Call writeBufferToHardware method
        buffer.writeBufferToHardware();

        // Verify that the data written to the ByteArrayOutputStream matches the expected binary representation of the page data for each page
        byte[] actualData = outputStream.toByteArray();

        // Expected binary representations of the mock page data
        byte[] expectedData1 = mockPage1.toBinary(Main.getCatalog().getTableSchema(mockPage1.getTableNumber()));
        byte[] expectedData2 = mockPage2.toBinary(Main.getCatalog().getTableSchema(mockPage2.getTableNumber()));

        // Verify that the data written to the ByteArrayOutputStream matches the expected binary representations of the page data
        
        boolean data1equv=expectedData1.equals(actualData);
        boolean data2equv=expectedData2.equals(actualData);
        System.out.println("Mock Page 1 Binary data is a match: "+data1equv);
        System.out.println("Mock Page 2 Binary data is a match: "+data2equv);
    }
    */
    
     
}
}
