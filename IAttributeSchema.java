public interface IAttributeSchema {
    public void setname(String name);
    public String getname();
    public void settype(String type);
    public String gettype();
    public void setunique(boolean unique);
    public boolean getunique();
    public void setnotnull(boolean nonNull);
    public boolean getnotnull();
    public void setprimarykey(boolean primarykey);
    public boolean getprimarykey();
    public void setsize(int size);
    public int getsize();
    public void displayAttribute(AttributeSchema attr);
}
