import java.util.ArrayList;
import java.util.List;

public class CartesianProduct {
    public static List<List<Record>> cartesianProduct(List<List<Record>> lists) {
        List<List<Record>> result = new ArrayList<>();
        cartesianProductHelper(lists, result, new ArrayList<>(), 0);
        return result;
    }

    private static void cartesianProductHelper(List<List<Record>> lists, List<List<Record>> result, List<Record> current, int index) {
        if (index == lists.size()) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (Record r : lists.get(index)) {
            current.add(r);
            cartesianProductHelper(lists, result, current, index + 1);
            current.remove(current.size() - 1);
        }
    }
}
