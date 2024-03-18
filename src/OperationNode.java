import java.util.ArrayList;

public class OperationNode {
    private OperandNode left;
    private String operator;
    private OperandNode right;

    public boolean evaluate() {
        Object leftValue = left.getValue();
        Object rightValue = right.getValue();
        
        String dataType = left.getType();
        
        // evaluate commparison based on data type and relational operator
        if (dataType.equalsIgnoreCase("integer")) {
            int leftInt = (int)leftValue;
            int rightInt = (int)rightValue;

            if (this.operator.equals("=")) return leftInt == rightInt;
            else if (this.operator.equals("<")) return leftInt < rightInt;
            else if (this.operator.equals(">")) return leftInt > rightInt;
            else if (this.operator.equals("<=")) return leftInt <= rightInt;
            else if (this.operator.equals(">=")) return leftInt >= rightInt;
            else if (this.operator.equals("!=")) return leftInt != rightInt;
        }
        else if (dataType.equalsIgnoreCase("double")) {
            double leftDouble = (double)leftValue;
            double rightDouble = (double)rightValue;

            if (this.operator.equals("=")) return leftDouble == rightDouble;
            else if (this.operator.equals("<")) return leftDouble < rightDouble;
            else if (this.operator.equals(">")) return leftDouble > rightDouble;
            else if (this.operator.equals("<=")) return leftDouble <= rightDouble;
            else if (this.operator.equals(">=")) return leftDouble >= rightDouble;
            else if (this.operator.equals("!=")) return leftDouble != rightDouble;
        }
        else if (dataType.equalsIgnoreCase("boolean")) {
            boolean leftBool = (boolean)leftValue;
            boolean rightBool = (boolean)rightValue;
            byte leftByte = (byte)(leftBool ? 1 : 0);
            byte rightByte = (byte)(rightBool ? 1 : 0);

            if (this.operator.equals("=")) return leftBool == rightBool;
            else if (this.operator.equals("<")) return leftByte < rightByte;
            else if (this.operator.equals(">")) return leftByte > rightByte;
            else if (this.operator.equals("<=")) return leftByte <= rightByte;
            else if (this.operator.equals(">=")) return leftByte >= rightByte;
            else if (this.operator.equals("!=")) return leftBool != rightBool;
        }
        else if (dataType.equalsIgnoreCase("char") || dataType.equalsIgnoreCase("varchar")) {
            String leftString = (String)leftValue;
            String rightString = (String)rightValue;
            int stringCmpResult = leftString.compareTo(rightString);

            if (this.operator.equals("=")) return stringCmpResult == 0;
            else if (this.operator.equals("<")) return stringCmpResult == -1;
            else if (this.operator.equals(">")) return stringCmpResult == 1;
            else if (this.operator.equals("<=")) return stringCmpResult == 0 || stringCmpResult == -1;
            else if (this.operator.equals(">=")) return stringCmpResult == 0 || stringCmpResult == 1;
            else if (this.operator.equals("!=")) return stringCmpResult != 0;
        }

        return false; //placeholder for compilation
    }

    // Checks for errors and passes values to attributes
    public boolean validate(TableSchema tableSchema, ArrayList<Object> tuple) {
        if (!left.validate(tableSchema, tuple)) return false;
        if (!right.validate(tableSchema, tuple)) return false;
        
        if (left.getType() != right.getType()) {
            System.err.println("Type of attribute " + left.getName() + " doesn't match type of " + right.getName());
            return false;
        }

        switch (this.operator) {
            case "=":
                break;
            case ">":
                break;
            case "<":
                break;
            case ">=":
                break;
            case "<=":
                break;
            case "!=":
                break;
            default:
                System.err.println(this.operator + " is invalid operator");
                return false;
        }

        //TODO: if attribute is nonNull, check if left or right equals agreed upon null value
        
        return true;
    }
}