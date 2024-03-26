import java.util.ArrayList;

public class OperationNode {
    private OperandNode left;
    private String operator;
    private OperandNode right;

    // Constructor to initialize operands and operator
    public OperationNode(OperandNode left, String operator, OperandNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    // Evaluates the operation based on the operator and operand types
    public boolean evaluate(Record record, TableSchema tableSchema) {
        Object leftValue = left.getValue(record, tableSchema); // getValue now needs Record and TableSchema
        Object rightValue = right.getValue(record, tableSchema); // getValue now needs Record and TableSchema

        // Ensure both operands have the same data type
        if (!left.getType().equalsIgnoreCase(right.getType())) {
            throw new IllegalArgumentException("Mismatched operand types: " + left.getType() + " and " + right.getType());
        }

        switch (left.getType().toLowerCase()) {
            case "integer":
                return evaluateIntegerOperation((Integer) leftValue, (Integer) rightValue);
            case "double":
                return evaluateDoubleOperation((Double) leftValue, (Double) rightValue);
            case "boolean":
                return evaluateBooleanOperation((Boolean) leftValue, (Boolean) rightValue);
            case "char":
            case "varchar":
                return evaluateStringOperation((String) leftValue, (String) rightValue);
            default:
                throw new IllegalArgumentException("Unsupported data type for operation: " + left.getType());
        }
    }

    // Validates the operation by checking the compatibility of operands and the operator
    public boolean validate(TableSchema tableSchema, ArrayList<Object> tuple) {
        if (!left.validate(tableSchema, tuple) || !right.validate(tableSchema, tuple)) {
            return false;
        }

        switch (operator) {
            case "=":
            case ">":
            case "<":
            case ">=":
            case "<=":
            case "!=":
                // Operator is valid, no action needed
                break;
            default:
                System.err.println("Invalid operator: " + operator);
                return false;
        }

        // TODO: Implement null value checks and unique constraints validation
        // if (left.isNonNull() && isNullValue(leftValue)) { ... }
        // if (right.isNonNull() && isNullValue(rightValue)) { ... }

        return true;
    }

    // Placeholder for method to check for null values
    private boolean isNullValue(Object value) {
        // Define what represents a null value in your context
        return value == null;
    }

    private boolean evaluateIntegerOperation(Integer leftInt, Integer rightInt) {
        switch (operator) {
            case "=":  return leftInt.equals(rightInt);
            case ">":  return leftInt > rightInt;
            case "<":  return leftInt < rightInt;
            case ">=": return leftInt >= rightInt;
            case "<=": return leftInt <= rightInt;
            case "!=": return !leftInt.equals(rightInt);
            default:   return false;
        }
    }

    private boolean evaluateDoubleOperation(Double leftDouble, Double rightDouble) {
        switch (operator) {
            case "=":  return leftDouble.equals(rightDouble);
            case ">":  return leftDouble > rightDouble;
            case "<":  return leftDouble < rightDouble;
            case ">=": return leftDouble >= rightDouble;
            case "<=": return leftDouble <= rightDouble;
            case "!=": return !leftDouble.equals(rightDouble);
            default:   return false;
        }
    }

    private boolean evaluateBooleanOperation(Boolean leftBool, Boolean rightBool) {
        if (operator.equals("=") || operator.equals("!=")) {
            return operator.equals("=") ? leftBool.equals(rightBool) : !leftBool.equals(rightBool);
        } else {
            throw new IllegalArgumentException("Invalid operator for boolean type: " + operator);
        }
    }

    private boolean evaluateStringOperation(String leftString, String rightString) {
        int comparisonResult = leftString.compareTo(rightString);

        switch (operator) {
            case "=":  return comparisonResult == 0;
            case ">":  return comparisonResult > 0;
            case "<":  return comparisonResult < 0;
            case ">=": return comparisonResult >= 0;
            case "<=": return comparisonResult <= 0;
            case "!=": return comparisonResult != 0;
            default:   return false;
        }
    }
}


