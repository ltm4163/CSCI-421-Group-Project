import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class AttributeSchema {
    private String name;
    private String type;
    private boolean unique;
    private boolean nonNull;
    private boolean primaryKey;
    private int size; // size of attr type

    private Object defaultValue;

    public AttributeSchema(String name, String type, boolean unique, boolean nonNull, boolean pK, int size) {
        this.name = name;
        this.type = type;
        this.unique = unique;
        this.nonNull = nonNull;
        this.primaryKey = pK;
        this.size = size;
        this.defaultValue = null;
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

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Object getDefaultValue() {
        return this.defaultValue;
    }

    public void displayAttribute() {
        System.out.println(this.name);
        System.out.print(this.type);
        if ("char".equalsIgnoreCase(this.type) || "varchar".equalsIgnoreCase(this.type)) {
            System.out.print("(" + this.size + ")");
        }
        if (this.unique) { System.out.print(" UNIQUE"); }
        if (this.nonNull) { System.out.print(" NOT NULL"); }
        if (this.primaryKey) { System.out.print(" PRIMARY KEY"); }
        System.out.println();
    }

    public boolean isUnique() {
        return this.unique;
    }

    public boolean isNonNull() {
        return this.nonNull;
    }

    public boolean isPrimaryKey() {
        return this.primaryKey;
    }

    public static AttributeSchema parse(String attributeString) {
        // Example input: "name varchar(255) unique notnull primarykey"
        String[] parts = attributeString.split("\\s+");
        System.out.println(Arrays.toString(parts));
        String name = parts[0];
        String type = parts[1];
        boolean unique = attributeString.contains("unique");
        boolean notNull = attributeString.contains("notnull");
        boolean primaryKey = attributeString.contains("primarykey");
        if (attributeString.contains("default")) {
            //setDefaultValue(parts[parts.length - 1]);
        }
        int size = 0;

        // Extract size for char and varchar types
        if (type.startsWith("char") || type.startsWith("varchar")) {
            size = Integer.parseInt(type.replaceAll("[^0-9]", ""));
            type = type.replaceAll("\\(.*\\)", ""); // Remove size from type string
        }

        return new AttributeSchema(name, type, unique, notNull, primaryKey, size);
    }

    public void writeToStream(DataOutputStream dos) throws IOException {
        dos.writeUTF(this.name);
        dos.writeUTF(this.type);
        dos.writeBoolean(this.unique);
        dos.writeBoolean(this.nonNull);
        dos.writeBoolean(this.primaryKey);
        dos.writeInt(this.size);
    }

    public static AttributeSchema readFromStream(DataInputStream dis) throws IOException {
        String name = dis.readUTF();
        String type = dis.readUTF();
        boolean unique = dis.readBoolean();
        boolean nonNull = dis.readBoolean();
        boolean primaryKey = dis.readBoolean();
        int size = dis.readInt();

        return new AttributeSchema(name, type, unique, nonNull, primaryKey, size);
    }
}
