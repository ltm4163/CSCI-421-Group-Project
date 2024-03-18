public interface ITableSchema {

    public void setnumAttributes(int numAttributes);
    public int getnumAttributes();
    public void setname(String name);
    public String getname();
    public void settableNumber(int tableNumber);
    public int gettableNumber();
    public void setAttributes(AttributeSchema[] attributes);
    public void displayTable(TableSchema table);
    public boolean hasPrimaryKey(TableSchema table);
}