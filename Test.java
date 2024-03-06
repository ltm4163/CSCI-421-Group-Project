import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Test {
    public static void testGetPage(Catalog catalog, StorageManager storageManager) {
        int int1 = 5;
        String string1 = "applebee";
        boolean flag1 = false;
        byte[] stringBytes1 = new byte[20];
        byte[] bytes1 = string1.getBytes();
        System.arraycopy(bytes1, 0, stringBytes1, 0, bytes1.length);
        String testString1 = new String(stringBytes1);
        System.out.println("testString: " + testString1);
        byte boolByte1 = (byte)(flag1 ? 1: 0);
        int numRecords = 1;
        int numPages = 1;
        ByteBuffer buffer = ByteBuffer.allocate((Integer.BYTES*3) + stringBytes1.length + 1);
        buffer.putInt(numPages);
        buffer.putInt(numRecords);
        buffer.putInt(int1);
        buffer.put(stringBytes1, 0, stringBytes1.length);
        buffer.put(boolByte1);
        byte[] pageAndNumRecs = new byte[Main.getPageSize()+Integer.BYTES];
        buffer.flip();
        buffer.get(pageAndNumRecs, 0, buffer.capacity());

        String fileName = Main.getDbDirectory() + "/tables/" + 0 + ".bin";
        try (RandomAccessFile fileIn = new RandomAccessFile(fileName, "rw")) {
            fileIn.write(pageAndNumRecs);
        } catch(IOException e) {
            e.printStackTrace();
        }

        AttributeSchema[] attributeSchemas = new AttributeSchema[3];
        AttributeSchema attr1 = new AttributeSchema("num", "integer", false, false, true, Integer.BYTES);
        AttributeSchema attr2 = new AttributeSchema("words", "char", false, false, false, stringBytes1.length);
        AttributeSchema attr3 = new AttributeSchema("flag", "boolean", false, false, false, 1);
        attributeSchemas[0] = attr1;
        attributeSchemas[1] = attr2;
        attributeSchemas[2] = attr3;
        TableSchema tableSchema = new TableSchema(3, "table0", 0, attributeSchemas);
        tableSchema.setNumPages(1);
        catalog.addTable(catalog, tableSchema);
        System.out.println("tableCount: " + catalog.tableCount);

        Page test = storageManager.getPage(0, 0);
        System.out.println("numRecords: " + test.getNumRecords());
        Record testRecord = test.getRecords().get(0);
        testRecord.getdata().flip();
        int testInt = testRecord.getdata().getInt();
        System.out.println("testInt: " + testInt);
        byte[] testStringBytes = new byte[20];
        testRecord.getdata().get(testStringBytes, 0, 20);
        String testString = new String(testStringBytes);
        System.out.println("testString: " + testString);
        byte testFlagByte = testRecord.getdata().get();
        boolean testFlag = (boolean)(testFlagByte == 1 ? true : false);
        System.out.println("testFlag: " + testFlag);
    }

    public static void testGetRecords(StorageManager storageManager) {
        storageManager.getRecords(0);
    }
}
