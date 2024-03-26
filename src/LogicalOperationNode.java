import java.util.ArrayList;

public class LogicalOperationNode {
    private WhereCondition leftCondition;
    private String logicalOperator; // "AND" or "OR"
    private WhereCondition rightCondition;

    public LogicalOperationNode(WhereCondition leftCondition, String logicalOperator, WhereCondition rightCondition) {
        this.leftCondition = leftCondition;
        this.logicalOperator = logicalOperator;
        this.rightCondition = rightCondition;
    }

    public boolean evaluate(Record record, TableSchema tableSchema) {
        boolean leftResult = leftCondition.evaluate(record, tableSchema);
        boolean rightResult = rightCondition.evaluate(record, tableSchema);

        switch (logicalOperator.toUpperCase()) {
            case "AND":
                return leftResult && rightResult;
            case "OR":
                return leftResult || rightResult;
            default:
                throw new IllegalArgumentException("Unsupported logical operator: " + logicalOperator);
        }
    }

    public boolean validate(TableSchema tableSchema, ArrayList<Object> tuple) {
        if (!leftCondition.validate(tableSchema) || !rightCondition.validate(tableSchema)) {
            return false;
        }

        switch (logicalOperator.toUpperCase()) {
            case "AND":
            case "OR":
                break;
            default:
                System.err.println("Invalid logical operator: " + logicalOperator);
                return false;
        }
        return true;
    }
}