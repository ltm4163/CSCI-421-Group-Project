import java.nio.ByteBuffer;

public interface IRecord {
    public void setdata(ByteBuffer data);
    public ByteBuffer getdata();
    public void setsize(int size);
    public int getsize();

}
