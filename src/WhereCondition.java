import java.util.Arrays;

class WhereCondition {
    enum Operator {
        EQUALS("="), NOT_EQUALS("!="), GREATER_THAN(">"), LESS_THAN("<"),
        GREATER_OR_EQUALS(">="), LESS_OR_EQUALS("<="), AND("AND"), OR("OR");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        public static Operator fromSymbol(String symbol) {
            for (Operator op : Operator.values()) {
                if (op.symbol.equals(symbol)) {
                    return op;
                }
            }
            throw new IllegalArgumentException("Unknown operator symbol: " + symbol);
        }
    }

    String attribute;
    Operator operator;
    String value;
    WhereCondition left;
    WhereCondition right;

    // Constructors for different node types
    public WhereCondition(String attribute, Operator operator, String value) {
        this.attribute = attribute;
        this.operator = operator;
        this.value = value;
    }

    public WhereCondition(WhereCondition left, Operator operator, WhereCondition right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public String toString() {
        if (this.operator == Operator.AND || this.operator == Operator.OR) {
            return "(" + left.toString() + " " + this.operator + " " + right.toString() + ")";
        } else {
            return this.attribute + " " + this.operator.symbol + " " + this.value;
        }
    }

    boolean evaluate(Record record, TableSchema tableSchema) {
        // Evaluate based on the type of operator
        switch (operator) {
            case AND:
                boolean andResult = left.evaluate(record, tableSchema) && right.evaluate(record, tableSchema);
                return andResult;
            case OR:
                boolean orResult = left.evaluate(record, tableSchema) || right.evaluate(record, tableSchema);
                return orResult;
            default:
                if (!tableSchema.contains(attribute)) {
                    return true;
                }
                Object recordValue = record.getAttributeValue(attribute, tableSchema.getattributes());
                boolean comparisonResult = compare(record, tableSchema, attribute, this.value, operator);
                return comparisonResult;
        }
    }

    private boolean compare(Record record, TableSchema tableSchema, String attribute, String value, Operator operator) {
        Object recordValue = record.getAttributeValue(attribute, tableSchema.getattributes());
        AttributeSchema attributeSchema = tableSchema.getAttributeByName(attribute);
    
        switch (attributeSchema.gettype().toLowerCase()) {
            case "integer":
                int recordValueInt = (Integer) recordValue;
                int valueInt = Integer.parseInt(value);
                return compareIntegers(recordValueInt, valueInt, operator);
            case "double":
                double recordValueDouble = (Double) recordValue;
                double valueDouble = Double.parseDouble(value);
                return compareDoubles(recordValueDouble, valueDouble, operator);
            case "char":
            case "varchar":
                String recordValueString = (String) recordValue;
                return compareStrings(recordValueString, value, operator);
            case "boolean":
                boolean recordValueBool = (Boolean) recordValue;
                boolean valueBool = Boolean.parseBoolean(value);
                return compareBooleans(recordValueBool, valueBool, operator);
            default:
                throw new IllegalArgumentException("Unsupported data type for comparison: " + attributeSchema.gettype());
        }
    }
    
    private boolean compareIntegers(int a, int b, Operator op) {
        switch (op) {
            case EQUALS: return a == b;
            case NOT_EQUALS: return a != b;
            case GREATER_THAN: return a > b;
            case LESS_THAN: return a < b;
            case GREATER_OR_EQUALS: return a >= b;
            case LESS_OR_EQUALS: return a <= b;
            default: throw new IllegalArgumentException("Unknown comparison operator: " + op);
        }
    }
    
    private boolean compareDoubles(double a, double b, Operator op) {
        final double EPSILON = 1E-6; // Define a small tolerance to account for floating-point arithmetic errors
        switch (op) {
            case EQUALS: return Math.abs(a - b) < EPSILON;
            case NOT_EQUALS: return Math.abs(a - b) >= EPSILON;
            case GREATER_THAN: return a > b + EPSILON;
            case LESS_THAN: return a + EPSILON < b;
            case GREATER_OR_EQUALS: return a > b - EPSILON;
            case LESS_OR_EQUALS: return a < b + EPSILON;
            default: throw new IllegalArgumentException("Unknown comparison operator: " + op);
        }
    }
    
    private boolean compareStrings(String a, String b, Operator op) {
        int comparison = a.compareTo(b);
        switch (op) {
            case EQUALS: return comparison == 0;
            case NOT_EQUALS: return comparison != 0;
            case GREATER_THAN: return comparison > 0;
            case LESS_THAN: return comparison < 0;
            case GREATER_OR_EQUALS: return comparison >= 0;
            case LESS_OR_EQUALS: return comparison <= 0;
            default: throw new IllegalArgumentException("Unknown comparison operator: " + op);
        }
    }
    
    private boolean compareBooleans(boolean a, boolean b, Operator op) {
        switch (op) {
            case EQUALS: return a == b;
            case NOT_EQUALS: return a != b;
            default: throw new IllegalArgumentException("Unsupported operation for boolean comparison: " + op);
        }
    }
    
    public boolean validate(TableSchema tableSchema) {
        // For binary logical operators, recursively validate left and right conditions
        if (operator == Operator.AND || operator == Operator.OR) {
            return left.validate(tableSchema) && right.validate(tableSchema);
        }
    
        // Check if the attribute exists in the table schema
        AttributeSchema attributeSchema = tableSchema.getAttributeByName(attribute);
        if (attributeSchema == null) {
            System.err.println("Attribute '" + attribute + "' does not exist in table schema.");
            return false;
        }
    
        // Check if the operation is compatible with the attribute's data type
        if (!isOperationCompatible(attributeSchema.gettype(), operator)) {
            System.err.println("Operator '" + operator.symbol + "' is not compatible with the data type of attribute '" + attribute + "'.");
            return false;
        }
    
        // Attempt to parse or convert the value to the attribute's type
        try {
            Object parsedValue = parseValueToType(value, attributeSchema.gettype());
            // Optionally, you could check further conditions here, such as range or format constraints
        } catch (IllegalArgumentException e) {
            System.err.println("Value '" + value + "' cannot be parsed or converted to the type of attribute '" + attribute + "': " + e.getMessage());
            return false;
        }
    
        return true;
    }
    
    private boolean isOperationCompatible(String dataType, Operator operator) {
        // Example implementation, this could be expanded based on your data types and what operations they support
        switch (dataType.toLowerCase()) {
            case "integer":
            case "double":
                return Arrays.asList(Operator.EQUALS, Operator.NOT_EQUALS, Operator.GREATER_THAN, Operator.LESS_THAN,
                        Operator.GREATER_OR_EQUALS, Operator.LESS_OR_EQUALS).contains(operator);
            case "boolean":
                return Arrays.asList(Operator.EQUALS, Operator.NOT_EQUALS).contains(operator);
            case "char":
            case "varchar":
                // Assuming all operations are allowed for strings, adjust as necessary
                return true;
            default:
                return false;
        }
    }
    
    private Object parseValueToType(String value, String dataType) throws NumberFormatException {
        // Convert value to appropriate type based on the attribute's data type
        switch (dataType.toLowerCase()) {
            case "integer":
                return Integer.parseInt(value);
            case "double":
                return Double.parseDouble(value);
            case "boolean":
                return Boolean.parseBoolean(value);
            case "char":
            case "varchar":
                // No conversion necessary for strings, but could validate length or format
                return value;
            default:
                throw new IllegalArgumentException("Unsupported data type: " + dataType);
        }
    }
}