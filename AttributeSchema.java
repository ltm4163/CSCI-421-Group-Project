

public class AttributeSchema implements IAttributeSchema{
    private String name;
    private String type;
    private boolean unique;
    private boolean nonNull;
    private boolean primaryKey;
    private int size; // size of attr type

    public AttributeSchema(String name, String type, boolean unique, boolean nonNull, boolean pK, int size) {
        this.name = name;
        this.type = type;
        this.unique = unique;
        this.nonNull = nonNull;
        this.primaryKey = pK;
        this.size = size;
    }
    public void setname(String name){
        this.name=name;
    }
    public String getname(){
        return this.name;
    }
    public void settype(String type){
        this.type=type;
    }
    public String gettype(){
        return this.type;
    }
    public void setunique(boolean unique){
        this.unique=unique;
    }
    public boolean getunique(){
        return this.unique;
    }
    public void setnotnull(boolean nonNull){
        this.nonNull=nonNull;
    }
    public boolean getnotnull(){
        return this.nonNull;
    }
    public void setprimarykey(boolean primarykey){
        this.primaryKey=primarykey;
    }
    public boolean getprimarykey(){
        return this.primaryKey;
    }
    public void setsize(int size){
        this.size=size;
    }
    public int getsize(){
        return this.size;
    }
    public void displayAttribute(AttributeSchema attr) {
        System.out.println(attr.getname());
    
        if((attr.gettype().equals("char")) || (attr.gettype().equals("varchar"))) {
            System.out.println(attr.gettype()+","+attr.getsize());
        } else { System.out.println(attr.gettype()); }
    
        if(attr.getunique()) { System.out.println(" unique "); }
        if(attr.getnotnull()) { System.out.println(" notNull"); }
        if(attr.getprimarykey()) { System.out.println(" primaryKey"); }
        System.out.println("\n");
    }
    public boolean isUnique() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isUnique'");
    }
    public boolean isNonNull() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isNonNull'");
    }
    public boolean isPrimaryKey() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isPrimaryKey'");
    }
}
