import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import java.util.ArrayList;
import java.util.Collections;

public class BPlusTest {

    public static void main(String[] args) {
        AttributeSchema attr1 = new AttributeSchema("num", "integer", false, false, true, Integer.BYTES);

        BPlusTree BPlusTest = new BPlusTree(attr1, 0);

        ArrayList<Byte> nullBitMap = new ArrayList<>(Collections.nCopies(2, (byte) 0));
        int int1 = 18;
        String string1 = "texty";
        ArrayList<Object> recTuple1 = new ArrayList<>();
        recTuple1.add(int1);
        recTuple1.add(string1);
        Record record1 = new Record(recTuple1, Integer.BYTES + 20, nullBitMap);

        for (int i = 1; i <= 1000000; i++) {
            BPlusTest.insert(record1, i, 0);
            BPlusTest.display();
        }

    }
}