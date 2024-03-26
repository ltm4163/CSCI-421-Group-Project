import java.util.ArrayList;

public interface OperandNode {
    public Object getValue(Record record, TableSchema tableSchema);
    public String getType();
    public String getName();
    public boolean validate(TableSchema tableSchema, ArrayList<Object> tuple); // Checks for errors and passes values to attributes
}
