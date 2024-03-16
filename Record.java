import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Record {
    private ArrayList<Object> data; // Tuple of data
    private int size;

    // Constructor
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
        int tupleIndex = 0; // Index of the current attribute in the tuple
    
        for (AttributeSchema attr : attributeSchemas) {
            switch (attr.gettype().toLowerCase()) {
                case "varchar":
                    // Handle varchar similar to the original code
                    String varcharValue = (String) this.data.get(tupleIndex);
                    byte[] varcharBytes = varcharValue.getBytes();
                    recData.put(varcharBytes);
                    break;
                case "char":
                    // Ensure that char attributes are handled according to their fixed length
                    String charValue = (String) this.data.get(tupleIndex);
                    // Pad the string with spaces to match the expected size or truncate if necessary
                    String paddedCharValue = String.format("%-" + attr.getsize() + "." + attr.getsize() + "s", charValue);
                    byte[] charBytes = paddedCharValue.getBytes();
                    recData.put(charBytes);
                    break;
                case "integer":
                    int intValue = (int) this.data.get(tupleIndex);
                    recData.putInt(intValue);
                    break;
                case "double":
                    double doubleValue = (double) this.data.get(tupleIndex);
                    recData.putDouble(doubleValue);
                    break;
                case "boolean":
                    boolean booleanValue = (boolean) this.data.get(tupleIndex);
                    byte booleanByte = (byte) (booleanValue ? 1 : 0);
                    recData.put(booleanByte);
                    break;
            }
            tupleIndex++;
        }
        return recData.array();
    }
    

    // Simplified getByteArray that directly calls toBinary assuming a schema is available
    public byte[] getByteArray(AttributeSchema[] attributeSchemas) {
        return toBinary(attributeSchemas); // Use the provided attribute schemas for serialization
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
