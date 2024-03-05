import java.nio.ByteBuffer;

public class Record implements IRecord {
    private ByteBuffer data;
    private int size;

    // Constructor
    public Record(ByteBuffer data, int size) {
        this.data = data;
        this.size = size;
    }
    public void setdata(ByteBuffer data)
    {
        this.data=data;
    }
    public ByteBuffer getdata()
    {
        return this.data;
    }
    public void setsize(int size){
        this.size=size;
    }
    public int getsize(){
        return this.size;
    }
    
    public Object getAttributeValue(String getname) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAttributeValue'");
    }
    public boolean matchesPrimaryKey(Object primaryKey) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'matchesPrimaryKey'");
    }

    public byte[] toBinary() {
        // Simple example: convert size to a 4-byte array
        return ByteBuffer.allocate(Integer.BYTES).putInt(size).array();
    }
}
