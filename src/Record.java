import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

public class Record {
    private ArrayList<Object> data;
    private int size;
    private ArrayList<Byte> nullBitMap;

    public Record(ArrayList<Object> data, int size, ArrayList<Byte> nullBitMap) {
        this.data = data;
        this.nullBitMap = nullBitMap;
        this.size = size + nullBitMap.size();
    }

    public void setData(ArrayList<Object> data) {
        this.data = data;
    }

    public ArrayList<Object> getData() {
        return this.data;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return this.size;
    }

   
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Record{");
        if (data != null && !data.isEmpty()) {
            for (int i = 0; i < data.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(data.get(i));
            }
        } else {
            sb.append("No Data");
        }
        sb.append('}');
        return sb.toString();
    }


    public void setBitMapValue(int index) {
        if (index >= this.nullBitMap.size()) {
            this.nullBitMap.add((byte)1);
            this.size++;
        }
        this.nullBitMap.set(index, (byte)1);
    }

    public byte getBitMapValue(int index) {
        if (nullBitMap.isEmpty() || index < 0 || index >= nullBitMap.size()) {
            throw new IllegalArgumentException("Index " + index + " is out of bounds for nullBitMap with size " + nullBitMap.size());
        }
        return nullBitMap.get(index);
    }
    

    public void setNullBitMap(ArrayList<Byte> nullBitMap) {
        this.nullBitMap = nullBitMap;
    }

    public Object getAttributeValue(String attributeName, AttributeSchema[] attributeSchemas) {
        for (int i = 0; i < attributeSchemas.length; i++) {
            if (attributeSchemas[i].getname().equals(attributeName)) {
                // Check if nullBitMap is initialized and has enough entries
                if (nullBitMap != null && i < nullBitMap.size() && getBitMapValue(i) == (byte)1) {
                    return "null"; // Return a string "null" or actual null, based on your handling preference
                } else {
                    // Ensure the data list is also properly initialized and has the entry
                    if (data != null && i < data.size()) {
                        return data.get(i);
                    } else {
                        // Data list not properly initialized or does not have the entry
                        return "ERROR: Data unavailable"; // Handle as appropriate for your application
                    }
                }
            }
        }
        throw new IllegalArgumentException("Attribute " + attributeName + " not found in record.");
    }
    

    public byte[] toBinary(AttributeSchema[] attributeSchemas) {
        ByteBuffer recData = ByteBuffer.allocate(this.size);
        int tupleIndex = 0; 

        byte[] bitMap = new byte[nullBitMap.size()];
        for (int i = 0; i < nullBitMap.size(); i++) {
            bitMap[i] = nullBitMap.get(i);
        }
        recData.put(bitMap);

        for (AttributeSchema attr : attributeSchemas) {
            if (this.getBitMapValue(tupleIndex) == (byte)1) {
                tupleIndex++;
                continue;
            }
            Object value = this.getData().get(tupleIndex);
            switch (attr.gettype().toLowerCase()) {
                case "varchar":
                    String varcharValue = (String) value;
                    byte[] varcharBytes = varcharValue.getBytes();
                    recData.putInt(varcharValue.length());
                    recData.put(varcharBytes);
                    break;
                case "char":
                    String charValue =  (String) value;
                    String paddedCharValue = String.format("%-" + attr.getsize() + "s", charValue);
                    byte[] charBytes = paddedCharValue.getBytes();
                    recData.put(charBytes);
                    break;
                case "integer":
                    int intValue = (int) value;
                    recData.putInt(intValue);
                    break;
                case "double":
                    double doubleValue = (double) value;
                    recData.putDouble(doubleValue);
                    break;
                case "boolean":
                    boolean booleanValue = (boolean) value;
                    byte booleanByte = (byte) (booleanValue ? 1 : 0);
                    recData.put(booleanByte);
                    break;
            }
            tupleIndex++;
        }
        return recData.array();
    }
    

    public byte[] getByteArray(AttributeSchema[] attributeSchemas) {
        return toBinary(attributeSchemas); 
    }

    public boolean matchesPrimaryKey(Object primaryKey, TableSchema tableSchema) {
        int primaryKeyIndex = -1;
        for (int i = 0; i < tableSchema.getattributes().length; i++) {
            if (tableSchema.getattributes()[i].isPrimaryKey()) {
                primaryKeyIndex = i;
                break;
            }
        }
        if (primaryKeyIndex == -1) {
            throw new IllegalStateException("No primary key defined for the table schema.");
        }
        Object pkValue = this.data.get(primaryKeyIndex);
        return pkValue.equals(primaryKey);
    }
}
