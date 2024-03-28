import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;

public class StorageManagerTest {
    public static void createTable1(Catalog catalog, int numPages) {
        AttributeSchema[] attributeSchemas = new AttributeSchema[3];
        AttributeSchema attr1 = new AttributeSchema("num", "integer", false, false, true, Integer.BYTES);
        AttributeSchema attr2 = new AttributeSchema("words", "char", false, false, false, 5);
        AttributeSchema attr3 = new AttributeSchema("flag", "boolean", false, false, false, 1);
        attributeSchemas[0] = attr1;
        attributeSchemas[1] = attr2;
        attributeSchemas[2] = attr3;
        TableSchema tableSchema = new TableSchema(3, "table0", 0, attributeSchemas);
        tableSchema.setNumPages(numPages);
        catalog.addTable(tableSchema);
    }

    public static void createTable2(Catalog catalog, int numPages) {
        AttributeSchema[] attributeSchemas = new AttributeSchema[3];
        AttributeSchema attr1 = new AttributeSchema("num", "integer", false, false, true, Integer.BYTES);
        AttributeSchema attr2 = new AttributeSchema("words", "char", true, false, false, 20);
        AttributeSchema attr3 = new AttributeSchema("flag", "boolean", false, false, false, 1);
        attributeSchemas[0] = attr1;
        attributeSchemas[1] = attr2;
        attributeSchemas[2] = attr3;
        TableSchema tableSchema = new TableSchema(3, "table0", 0, attributeSchemas);
        tableSchema.setNumPages(numPages);
        catalog.addTable(tableSchema);
    }

    public static void createNonIntTables(Catalog catalog) {
        AttributeSchema[] attributeSchemas = new AttributeSchema[2];
        AttributeSchema attr1 = new AttributeSchema("num", "integer", false, false, false, Integer.BYTES);
        AttributeSchema attr2 = new AttributeSchema("words", "char", false, false, true, 20);
        attributeSchemas[0] = attr1;
        attributeSchemas[1] = attr2;
        TableSchema tableSchema = new TableSchema(2, "table0", 0, attributeSchemas);
        catalog.addTable(tableSchema);

        AttributeSchema[] attributeSchema1 = new AttributeSchema[2];
        AttributeSchema attr3 = new AttributeSchema("num", "double", false, false, true, Double.BYTES);
        AttributeSchema attr4 = new AttributeSchema("words", "char", false, false, false, 20);
        attributeSchema1[0] = attr3;
        attributeSchema1[1] = attr4;
        TableSchema tableSchema1 = new TableSchema(2, "table1", 1, attributeSchema1);
        catalog.addTable(tableSchema1);

        AttributeSchema[] attributeSchema2 = new AttributeSchema[2];
        AttributeSchema attr5 = new AttributeSchema("flag", "boolean", false, false, false, 1);
        AttributeSchema attr6 = new AttributeSchema("words", "varchar", false, false, true, 20);
        attributeSchema2[0] = attr5;
        attributeSchema2[1] = attr6;
        TableSchema tableSchema2 = new TableSchema(2, "table2", 2, attributeSchema2);
        catalog.addTable(tableSchema2);

        AttributeSchema[] attributeSchema3 = new AttributeSchema[2];
        AttributeSchema attr7 = new AttributeSchema("flag", "boolean", false, false, true, 1);
        AttributeSchema attr8 = new AttributeSchema("words", "varchar", false, false, false, 20);
        attributeSchema3[0] = attr7;
        attributeSchema3[1] = attr8;
        TableSchema tableSchema3 = new TableSchema(2, "table3", 3, attributeSchema3);
        catalog.addTable(tableSchema3);
    }

    public static void testNonIntPK(StorageManager storageManager, Catalog catalog) {
        createNonIntTables(catalog);

        // table 0
        int int0 = 2;
        String string0 = "texty";
        ArrayList<Object> recTuple0 = new ArrayList<>();
        ArrayList<Byte> nullBitMap = new ArrayList<>(Collections.nCopies(2, (byte) 0));
        recTuple0.add(int0);
        recTuple0.add(string0);
        Record record0 = new Record(recTuple0, Integer.BYTES + 20, nullBitMap);

        int int1 = 18;
        String string1 = "texty";
        ArrayList<Object> recTuple1 = new ArrayList<>();
        recTuple1.add(int1);
        recTuple1.add(string1);
        Record record1 = new Record(recTuple1, Integer.BYTES + 20, nullBitMap);

        storageManager.addRecord(catalog, record0, 0);
        storageManager.addRecord(catalog, record1, 0);
        parser.printAttributeNames(catalog.getTableSchema(0).getattributes());
        ArrayList<ArrayList<Object>> tuples = storageManager.getRecords(0);
        for(ArrayList<Object> tuple : tuples) {
            parser.printTuple(tuple, catalog.getTableSchema(0).getattributes());
        }

        // table 1
        double double0 = 7.5;
        String string2 = "blasdmaio";
        ArrayList<Object> recTuple2 = new ArrayList<>();
        recTuple2.add(double0);
        recTuple2.add(string2);
        Record record2 = new Record(recTuple2, Double.BYTES + 20, nullBitMap);

        double double1 = 3.5;
        String string3 = "dnuiqb";
        ArrayList<Object> recTuple3 = new ArrayList<>();
        recTuple3.add(double1);
        recTuple3.add(string3);
        Record record3 = new Record(recTuple3, Double.BYTES + 20, nullBitMap);

        storageManager.addRecord(catalog, record2, 1);
        storageManager.addRecord(catalog, record3, 1);
        parser.printAttributeNames(catalog.getTableSchema(1).getattributes());
        ArrayList<ArrayList<Object>> tuples1 = storageManager.getRecords(1);
        for(ArrayList<Object> tuple : tuples1) {
            parser.printTuple(tuple, catalog.getTableSchema(1).getattributes());
        }

        // table 2
        boolean boolean0 = true;
        String string4 = "teestst";
        ArrayList<Object> recTuple4 = new ArrayList<>();
        recTuple4.add(boolean0);
        recTuple4.add(string4);
        Record record4 = new Record(recTuple4, 1 + string4.length(), nullBitMap);

        boolean boolean1 = true;
        String string5 = "teestst";
        ArrayList<Object> recTuple5 = new ArrayList<>();
        recTuple5.add(boolean1);
        recTuple5.add(string5);
        Record record5 = new Record(recTuple5, 1 + string5.length(), nullBitMap);

        storageManager.addRecord(catalog, record4, 2);
        storageManager.addRecord(catalog, record5, 2);
        parser.printAttributeNames(catalog.getTableSchema(2).getattributes());
        ArrayList<ArrayList<Object>> tuples2 = storageManager.getRecords(2);
        for(ArrayList<Object> tuple : tuples2) {
            parser.printTuple(tuple, catalog.getTableSchema(2).getattributes());
        }

        // table 3
        boolean boolean2 = true;
        String string6 = "teestst";
        ArrayList<Object> recTuple6 = new ArrayList<>();
        recTuple6.add(boolean2);
        recTuple6.add(string6);
        Record record6 = new Record(recTuple6, 1 + string6.length(), nullBitMap);

        boolean boolean3 = true;
        String string7 = "befoe";
        ArrayList<Object> recTuple7 = new ArrayList<>();
        recTuple7.add(boolean3);
        recTuple7.add(string7);
        Record record7 = new Record(recTuple7, 1 + string7.length(), nullBitMap);

        storageManager.addRecord(catalog, record6, 3);
        storageManager.addRecord(catalog, record7, 3);
        parser.printAttributeNames(catalog.getTableSchema(3).getattributes());
        ArrayList<ArrayList<Object>> tuples3 = storageManager.getRecords(3);
        for(ArrayList<Object> tuple : tuples3) {
            parser.printTuple(tuple, catalog.getTableSchema(3).getattributes());
        }
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
        System.out.println("tableCount: " + catalog.getTableCount());

        Page test = storageManager.getPage(0, 0);
        System.out.println("numRecords: " + test.getNumRecords());
        Record testRecord = test.getRecords().get(0);
        int testInt = (int)testRecord.getData().get(0);
        System.out.println("testInt: " + testInt);
        String testString = (String)testRecord.getData().get(1);
        System.out.println("testString: " + testString);
        boolean testFlag = (boolean)testRecord.getData().get(2);
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
        //createTable1(catalog, 0);
        createTable2(catalog, 0);
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
        String string2 = "larger string";
        boolean flag2 = true;
        ArrayList<Object> recTuple2 = new ArrayList<>();
        ArrayList<Byte> nullBitMap = new ArrayList<>(Collections.nCopies(3, (byte) 0));
        recTuple2.add(int2);
        recTuple2.add(string2);
        recTuple2.add(flag2);

        Record record = new Record(recTuple, Integer.BYTES + 20 + 1, nullBitMap);
        Record record2 = new Record(recTuple2, Integer.BYTES + 20 + 1, nullBitMap);
        storageManager.addRecord(catalog, record, 0);
        storageManager.addRecord(catalog, record2, 0);
        parser.printAttributeNames(catalog.getTableSchema(0).getattributes());
        ArrayList<ArrayList<Object>> tuples = storageManager.getRecords(0);
        for(ArrayList<Object> tuple : tuples) {
            parser.printTuple(tuple, catalog.getTableSchema(0).getattributes());
        }
    }
}