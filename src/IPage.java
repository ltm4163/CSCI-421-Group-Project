public interface IPage {
    public Record[] getRecords();
    public void setRecords(Record[] records);
    public Object getData();
    public void setData(Object data);
    public int getPageNumber();
    public void setPageNumber(int pageNumber);
    public int getTableNumber();
    public void setTableNumber(int tableNumber);
    public int getNumRecords();
    public void setNumRecords(int numRecords);
    public int getSize();
    public void setSize(int size);
    public boolean isUpdated();
    public void setUpdated(boolean updated);
}
