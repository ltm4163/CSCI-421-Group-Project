public class Constants implements IConstants{
    
    public static final int MAX_NUM_ATTRIBUTES = 10;
    public static final int MAX_NAME_SIZE = 255;
    public static final int MAX_NUM_RECORDS = 100;
    
    public static final int MAX_PAGE_SIZE = 4096; // bytes

    public static int maxPageSize = 42;
    public static int maxBufferSize = 10;

    
    public void updateValues(int pageSize, int bufferSize) {
        Constants.maxPageSize = pageSize;
        Constants.maxBufferSize = bufferSize;
    }
    
}  

        
    
  

