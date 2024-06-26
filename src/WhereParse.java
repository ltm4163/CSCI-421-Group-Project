import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WhereParse {
    public record Condition(String leftColumn, String operator, String rightColumn, String value) {

        @Override
        public String toString() {
            if (rightColumn != null) {
                return leftColumn + " " + operator + " " + rightColumn;
            } else {
                return leftColumn + " " + operator + " " + value;
            }
        }
    }

    private static int findOperatorIndex(String input) {
        // Array of operators to search for
        String[] operators = {"=", "!=", ">", "<", ">=", "<="};

        // Loop through each operator
        for (String operator : operators) {
            int index = input.indexOf(operator);
            // If operator is found, return its index
            if (index != -1) {
                return index;
            }
        }
        // If none of the operators are found, return -1
        return -1;
    }

    static WhereCondition parseWhereClause(String whereClause, List<TableSchema> tableSchemas) {
        if (whereClause == null || whereClause.trim().isEmpty()) {
            System.err.println("parseWhereClause: WHERE clause is null or empty.");
            return null;
        }

        final Pattern conditionPattern = Pattern.compile("(\\w+)\\s*(=|!=|<|>|<=|>=)\\s*('?\\w+'?|\\d+(\\.\\d+)?)");
        final Pattern logicalPattern = Pattern.compile("\\s+(AND|OR)\\s+", Pattern.CASE_INSENSITIVE);

        List<String> logicalOperators = new ArrayList<>();
        Matcher logicalMatcher = logicalPattern.matcher(whereClause);
        while (logicalMatcher.find()) {
            logicalOperators.add(logicalMatcher.group(1).toUpperCase());
        }

        String[] conditions = logicalPattern.split(whereClause);
        WhereCondition rootCondition = null;

        for (int i = 0; i < conditions.length; i++) {
            int indexOfConditional = findOperatorIndex(conditions[i]);
            String conditionTableAttribute = conditions[i].substring(0, indexOfConditional).trim();
            String column = conditions[i].substring(conditionTableAttribute.indexOf('.') + 1, indexOfConditional).trim();
            if (conditions[i].indexOf('.') == -1) {
                List<TableSchema> foundInTables = new ArrayList<>();
                for (TableSchema table : tableSchemas) {
                    List<String> attributeNames = table.getAttributeNames();
                    if (attributeNames.contains(column)) {
                        foundInTables.add(table);
                    }
                }
                if (foundInTables.size() > 1) {
                    System.out.println("Column '" + column + "' is present in multiple tables: " + foundInTables);
                    return null;
                }
            }

//            String stringToUse;
//            if (conditionTableAttribute.indexOf('.') == -1) {
//                stringToUse = conditions[i];
//            }
//            else {
//                stringToUse = conditions[i].substring(conditions[i].indexOf('.') + 1);
//            }
            Matcher conditionMatcher = conditionPattern.matcher(conditions[i].trim());
            if (!conditionMatcher.matches()) {
                System.err.println("parseWhereClause: Failed to match condition pattern in part: " + conditions[i]);
                continue;
            }

            if (!conditionMatcher.matches()) {
                System.err.println("Failed to match condition pattern in part: " + conditions[i]);
                continue;
            }

            String attribute = conditionMatcher.group(1);
            String operator = conditionMatcher.group(2);
            String value = conditionMatcher.group(3).replace("'", ""); // Removing single quotes
            WhereCondition.Operator parsedOperator = WhereCondition.Operator.fromSymbol(operator);
            if (parsedOperator == null) {
                System.err.println("parseWhereClause: Unknown operator: " + operator);
                continue;
            }

            WhereCondition currentCondition = new WhereCondition(attribute, parsedOperator, value);
            if (rootCondition == null) {
                rootCondition = currentCondition;
            } else if (i - 1 < logicalOperators.size()) {
                String logicalOp = logicalOperators.get(i - 1);
                WhereCondition.Operator logicalOperator = WhereCondition.Operator.fromSymbol(logicalOp);
                if (logicalOperator == null) {
                    System.err.println("parseWhereClause: Unknown logical operator: " + logicalOp);
                    continue;
                }
                rootCondition = new WhereCondition(rootCondition, logicalOperator, currentCondition);
            }
        }

        if (rootCondition == null) {
            System.err.println("parseWhereClause: Root condition is null after parsing, indicating an error in the WHERE clause.");
        }

        return rootCondition;
    }
}