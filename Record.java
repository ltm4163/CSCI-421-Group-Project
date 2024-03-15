import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Record implements IRecord {
    private ArrayList<Object> data; //tuple of data
    private int size;

    // Constructor
    public Record(ArrayList<Object> data, int size) {
        this.data = data;
        this.size = size;
    }
    public void setdata(ArrayList<Object> data)
    {
        this.data=data;
    }
    public ArrayList<Object> getdata()
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
        // int sizeToRead = attr.getsize(); //used to tell how much data to read from page.data
        // if (attr.getname().equals("varchar")) { //if type is varchar, read int that tells length of varchar
        //     sizeToRead = this.getdata().getInt();
        // }
        // byte[] attrValueBytes = new byte[sizeToRead];
        // this.getdata().get(attrValueBytes, 0, sizeToRead);
        // return attrValueBytes;
        return null;
    }

    public boolean matchesPrimaryKey(Object primaryKey) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'matchesPrimaryKey'");
    }

    public byte[] toBinary(AttributeSchema[] attributeSchemas) {
        ByteBuffer recData = ByteBuffer.allocate(getsize());
        int tupleIndex = 0; // index of current attribute in tuple
        for (AttributeSchema attr : attributeSchemas) {
            if (attr.gettype().equalsIgnoreCase("varchar")) {
                String attrValue = (String)this.getdata().get(tupleIndex);
                byte[] attrBytes = attrValue.getBytes();
                recData.put(attrBytes);
            }
            else if (attr.gettype().equalsIgnoreCase("char")) {
                String attrValue = (String)this.getdata().get(tupleIndex);
                byte[] attrBytes = attrValue.getBytes();
                recData.put(attrBytes);
            }
            else if (attr.gettype().equalsIgnoreCase("integer")) {
                int attrValue = (int)this.getdata().get(tupleIndex);
                recData.putInt(attrValue);
            }
            else if (attr.gettype().equalsIgnoreCase("double")) {
                double attrValue = (double)this.getdata().get(tupleIndex);
                recData.putDouble(attrValue);
            }
            else if (attr.gettype().equalsIgnoreCase("boolean")) {
                boolean attrValue = (boolean)this.getdata().get(tupleIndex);
                byte attrValueByte = (byte)(attrValue ? 1 : 0);
                recData.put(attrValueByte);
            }
            tupleIndex++;
        }
        return recData.array();
    }
}