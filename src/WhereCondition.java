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
                System.out.println("Debug: AND Evaluation - " + andResult);
                return andResult;
            case OR:
                boolean orResult = left.evaluate(record, tableSchema) || right.evaluate(record, tableSchema);
                System.out.println("Debug: OR Evaluation - " + orResult);
                return orResult;
            default:
                Object recordValue = record.getAttributeValue(attribute, tableSchema.getattributes());
                boolean comparisonResult = compare(record, tableSchema, attribute, this.value, operator);
                System.out.println("Debug: Comparing - Attribute: " + attribute + ", Record Value: " + recordValue + ", Condition Value: " + this.value + ", Result: " + comparisonResult);
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
                switch (operator) {
                    case EQUALS:
                        return recordValueInt == valueInt;
                    case NOT_EQUALS:
                        return recordValueInt != valueInt;
                    case GREATER_THAN:
                        return recordValueInt > valueInt;
                    case LESS_THAN:
                        return recordValueInt < valueInt;
                    // Add other cases
                }
                break;
            // TODO: Handle other types similarly
        }
        throw new IllegalArgumentException("Unsupported operation for condition.");
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
