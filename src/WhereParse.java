import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
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

    static WhereCondition parseWhereClause(String whereClause) {
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
            Matcher conditionMatcher = conditionPattern.matcher(conditions[i].trim());
            if (!conditionMatcher.matches()) {
                System.err.println("parseWhereClause: Failed to match condition pattern in part: " + conditions[i]);
                continue;
            }

            System.out.println("Attempting to match condition: " + conditions[i].trim());
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
        } else {
            System.out.println("parseWhereClause: Successfully parsed WHERE condition.");
        }

        return rootCondition;
    }

//    public static List<List<Condition>> parseWhereClause(String whereClause) {
//        List<List<Condition>> subClauses = new ArrayList<>();
//
//        // Split WHERE clause by AND operator; keeps OR operations together
//        String[] clauses = whereClause.split("(?i)\\s+AND\\s+");
//
//        for (String clause : clauses) {
//            List<Condition> conditions = new ArrayList<>();
//
//            // Define regex patterns for column, operator, and value
//            String columnPattern = "[a-zA-Z_][a-zA-Z0-9_]*";
//            String operatorPattern = "=|<|>|<=|>=|!=";
//            String valuePattern = "'.*?'|\".*?\"|\\d+";
//
//            // Combine patterns to create a complete regex pattern for a condition
//            String conditionPattern = "\\s*(" + columnPattern + ")\\s*(" + operatorPattern + ")\\s*((" + columnPattern + ")|(" + valuePattern + "))\\s*";
//
//            // Compile the regex pattern
//            Pattern pattern = Pattern.compile(conditionPattern);
//            Matcher matcher = pattern.matcher(clause);
//
//            // Iterate through matches and create conditions
//            while (matcher.find()) {
//                String leftColumn = matcher.group(1);
//                String operator = matcher.group(2);
//                String rightColumn = matcher.group(4);
//                String value = matcher.group(5);
//                conditions.add(new Condition(leftColumn, operator, rightColumn, value));
//            }
//
//            if (!conditions.isEmpty()) {
//                subClauses.add(conditions);
//            }
//        }
//
//        return subClauses;
//    }
}

// select * from table0 where age >= 18 AND (name = 'John' OR city = 'New York' OR age = salary) -- works
//      should output [[name = 'John', city = 'New York', age = salary], [age >= 18]]
// select * from table0 where (name = 'John' OR city = 'New York' OR age = salary) AND age >= 18 -- works
//      should output [[name = 'John', city = 'New York', age = salary], [age >= 18]]
// select * from table0 where (name = 'John' OR (city = 'New York' AND age = salary)) AND age >= 18 -- doesn't work
//      should output [[name = 'John', [[city = 'New York'], [age = salary]]], [age >= 18]], or something to that nature