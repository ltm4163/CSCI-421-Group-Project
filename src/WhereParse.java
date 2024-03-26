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

    public static List<List<Condition>> parseWhereClause(String whereClause) {
        List<List<Condition>> subClauses = new ArrayList<>();

        // Split WHERE clause by AND operator; keeps OR operations together
        String[] clauses = whereClause.split("(?i)\\s+AND\\s+");

        for (String clause : clauses) {
            List<Condition> conditions = new ArrayList<>();

            // regex patterns for column, operator, and value
            String columnPattern = "[a-zA-Z_][a-zA-Z0-9_]*";
            String operatorPattern = "=|<|>|<=|>=|!=";
            String valuePattern = "'.*?'|\".*?\"|\\d+";

            // complete regex pattern for a condition
            String conditionPattern = "\\s*(" + columnPattern + ")\\s*(" + operatorPattern + ")\\s*((" + columnPattern + ")|(" + valuePattern + "))\\s*";

            Pattern pattern = Pattern.compile(conditionPattern);
            Matcher matcher = pattern.matcher(clause);

            // Iterate through matches and create conditions
            while (matcher.find()) {
                String leftColumn = matcher.group(1);
                String operator = matcher.group(2);
                String rightColumn = matcher.group(4);
                String value = matcher.group(5);
                conditions.add(new Condition(leftColumn, operator, rightColumn, value));
            }

            if (!conditions.isEmpty()) {
                subClauses.add(conditions);
            }
        }

        return subClauses;
    }
}