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
    
    public byte[] getAttributeValue(AttributeSchema attr) {
        int sizeToRead = attr.getsize(); //used to tell how much data to read from page.data
        if (attr.getname().equals("varchar")) { //if type is varchar, read int that tells length of varchar
            sizeToRead = this.getdata().getInt();
        }
        byte[] attrValueBytes = new byte[sizeToRead];
        this.getdata().get(attrValueBytes, 0, sizeToRead);
        return attrValueBytes;
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
