package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ListUtils {
    public static <T, K> boolean isNotUnique(List<T> xs, Function<T, K> key) {
        var count = xs.stream().map(key).distinct().count();
        return count != xs.size();
    }

    public static <T> List<T> concat(List<T> xs, List<T> ys) {
        var result = new ArrayList<T>();
        result.addAll(xs);
        result.addAll(ys);
        return result;
    }
}
