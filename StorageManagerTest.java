import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class StorageManagerTest {
    public static void createTable1(Catalog catalog, int numPages) {
        AttributeSchema[] attributeSchemas = new AttributeSchema[3];
        AttributeSchema attr1 = new AttributeSchema("num", "integer", false, false, true, Integer.BYTES);
        AttributeSchema attr2 = new AttributeSchema("words", "char", false, false, false, 20);
        AttributeSchema attr3 = new AttributeSchema("flag", "boolean", false, false, false, 1);
        attributeSchemas[0] = attr1;
        attributeSchemas[1] = attr2;
        attributeSchemas[2] = attr3;
        TableSchema tableSchema = new TableSchema(3, "table0", 0, attributeSchemas);
        tableSchema.setNumPages(numPages);
        catalog.addTable(catalog, tableSchema);
    }
    
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
        ByteBuffer buffer = ByteBuffer.allocate((Integer.BYTES*3) + 20 + 1);
        buffer.putInt(numPages);
        buffer.putInt(numRecords);
        buffer.putInt(int1);
        buffer.put(stringBytes1, 0, 20);
        buffer.put(boolByte1);
        byte[] pageAndNumRecs = new byte[Main.getPageSize()+(Integer.BYTES*2)];
        buffer.rewind();
        buffer.get(pageAndNumRecs, 0, buffer.capacity());

        String fileName = Main.getDbDirectory() + "/tables/" + 0 + ".bin";
        try (RandomAccessFile fileIn = new RandomAccessFile(fileName, "rw")) {
            fileIn.write(pageAndNumRecs);
        } catch(IOException e) {
            e.printStackTrace();
        }

        createTable1(catalog, 1);
        System.out.println("tableCount: " + catalog.tableCount);

        Page test = storageManager.getPage(0, 0);
        System.out.println("numRecords: " + test.getNumRecords());
        Record testRecord = test.getRecords().get(0);
        int testInt = (int)testRecord.getdata().get(0);
        System.out.println("testInt: " + testInt);
        String testString = (String)testRecord.getdata().get(1);
        System.out.println("testString: " + testString);
        boolean testFlag = (boolean)testRecord.getdata().get(2);
        System.out.println("testFlag: " + testFlag);
    }

    // call testGetPage first
    public static void testGetRecords(Catalog catalog, StorageManager storageManager) {
        parser.printAttributeNames(catalog.getTableSchema(0).getattributes());
        ArrayList<ArrayList<Object>> tuples = storageManager.getRecords(0);
        for(ArrayList<Object> tuple : tuples) {
            parser.printTuple(tuple, catalog.getTableSchema(0).getattributes());
        }
    }

    // public static void testLoadFromDisk(StorageManager storageManager) {
    //     Main.getBuffer().writeBufferToHardware();
   //     Page page = storageManager.loadPageFromDisk(0, 1);
    //     Record rec = page.getRecords().get(0);
    //     System.out.println("int2: " + (int)rec.getdata().get(0));
    // }

    public static void testInsert(Catalog catalog, StorageManager storageManager) {
        createTable1(catalog, 0);
        // System.out.println("isPK: " + tableSchema.getattributes()[0].getprimarykey());
        System.out.println("testing insert");

        int int1 = 15;
        String string1 = "larger string";
        boolean flag1 = false;
        ArrayList<Object> recTuple = new ArrayList<>();
        recTuple.add(int1);
        recTuple.add(string1);
        recTuple.add(flag1);
        int int2 = 11;
        String string2 = "largest string";
        boolean flag2 = true;
        ArrayList<Object> recTuple2 = new ArrayList<>();
        recTuple2.add(int2);
        recTuple2.add(string2);
        recTuple2.add(flag2);

        Record record = new Record(recTuple, Integer.BYTES + 20 + 1);
        Record record2 = new Record(recTuple2, Integer.BYTES + 20 + 1);
        storageManager.addRecord(catalog, record, 0);
        storageManager.addRecord(catalog, record2, 0);
        parser.printAttributeNames(catalog.getTableSchema(0).getattributes());
        ArrayList<ArrayList<Object>> tuples = storageManager.getRecords(0);
        for(ArrayList<Object> tuple : tuples) {
            parser.printTuple(tuple, catalog.getTableSchema(0).getattributes());
        }
    }
}