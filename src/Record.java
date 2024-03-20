import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Record {
    private ArrayList<Object> data;
    private int size;

    public Record(ArrayList<Object> data, int size) {
        this.data = data;
        this.size = size;
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

    public byte[] toBinary(AttributeSchema[] attributeSchemas) {
        ByteBuffer recData = ByteBuffer.allocate(this.size);
        int tupleIndex = 0; 
    
        for (AttributeSchema attr : attributeSchemas) {
            Object value = this.getData().get(tupleIndex);
            switch (attr.gettype().toLowerCase()) {
                case "varchar":
                    String varcharValue = "NULL";
                    if (value != null) varcharValue = (String) value;
                    byte[] varcharBytes = varcharValue.getBytes();
                    recData.putInt(varcharValue.length());
                    recData.put(varcharBytes);
                    break;
                case "char":
                    String charValue = "NULL";
                    if (value != null) charValue = (String) value;
                    String paddedCharValue = String.format("%-" + attr.getsize() + "." + attr.getsize() + "s", charValue);
                    byte[] charBytes = paddedCharValue.getBytes();
                    recData.put(charBytes);
                    break;
                case "integer":
                    int intValue = -1;
                    if (value != null) intValue = (int) value;
                    recData.putInt(intValue);
                    break;
                case "double":
                    double doubleValue = -1.0;
                    if (value != null) doubleValue = (double) value;
                    recData.putDouble(doubleValue);
                    break;
                case "boolean":
                    byte booleanByte = -1;
                    if (value != null) {
                        boolean booleanValue = (boolean) value;
                        booleanByte = (byte) (booleanValue ? 1 : 0);
                    }
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
