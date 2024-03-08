import java.nio.ByteBuffer;
import java.util.ArrayList;

public interface IRecord {
    public void setdata(ArrayList<Object> data);
    public ArrayList<Object> getdata();
    public void setsize(int size);
    public int getsize();

}