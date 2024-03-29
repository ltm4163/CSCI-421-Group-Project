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

    public int addValue(Object value, int indexInRecord, AttributeSchema attr) {
        int sizeAdded = 1;
        if (value == null) this.setBitMapValue(indexInRecord, 1);
        else {
            this.setBitMapValue(indexInRecord, 0);
            switch (attr.gettype()) {
                case "varchar":
                    sizeAdded += ((String)value).length() + Integer.BYTES;
                    break;
                default:
                    sizeAdded += attr.getsize();
                    break;
            }
        }
        this.data.add(value);
        this.size += sizeAdded;
        return sizeAdded;
    }

    public int removeValue(int indexInRecord, AttributeSchema attr) {
        Object value = this.data.remove(indexInRecord);
        this.nullBitMap.remove(indexInRecord);
        int sizeLost = 1;
        switch (attr.gettype()) {
            case "varchar":
                sizeLost += (((String)value).length() + Integer.BYTES);
                break;
            default:
                sizeLost += attr.getsize();
                break;
        }
        this.size -= sizeLost;
        return sizeLost;
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
  
    public void setBitMapValue(int index, int isNull) {
        if (index >= this.nullBitMap.size()) {
            this.nullBitMap.add((byte)isNull);
            this.size++;
        }
        else this.nullBitMap.set(index, (byte)isNull);
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
  
  public int getNumElements() {
      return this.data.size();
  }

    public Object getAttributeValue(String attributeName, AttributeSchema[] attributeSchemas) {
        String[] parts = attributeName.split("\\.");
        String actualAttributeName = parts.length > 1 ? parts[1] : parts[0];
    
        for (AttributeSchema attr : attributeSchemas) {
            if (attr.getname().equals(actualAttributeName)) {
                int index = Arrays.asList(attributeSchemas).indexOf(attr);
                if (index < 0 || index >= data.size()) {
                    System.err.println("Error: Attribute index out of bounds. Attribute: " + actualAttributeName);
                    return null; 
                }
                return data.get(index);
            }
        }
        System.err.println("Error: Attribute " + actualAttributeName + " not found in record.");
        return null; 
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