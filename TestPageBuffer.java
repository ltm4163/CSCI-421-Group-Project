import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
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
     
}
