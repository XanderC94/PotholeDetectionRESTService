package utils;

import java.util.Collection;
import java.util.stream.Collectors;

public class Utils {

    public static String stringify(final String str) { return "'" + str + "'";}

    public static void println(String x) { System.out.println(x); }

    public static <T extends Object> void println(Collection<T> x) {
        System.out.println(
                String.join(",",
                        x.stream().map(Object::toString).collect(Collectors.toList())
                )
        );
    }

    public static <T extends Object> String collectionToString(Collection<T> x) {
        return String.join(",",
                x.stream().map(Object::toString).collect(Collectors.toList())
        );
    }
}
