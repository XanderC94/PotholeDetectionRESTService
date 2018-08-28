package utils;

import json.GeoCoordinates;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {

    public static final Pattern addressRegex = Pattern.compile("(?:\"address\":)\\{(.*?)\\}");
    public static final Pattern coordinatesRegex = Pattern.compile("\"lat\":\"[+-]?\\d*\\.\\d*\",\"lon\":\"[+-]?\\d*\\.\\d*\"");
    public static final Pattern routingRegex = Pattern.compile("(?:\"geometry\":\\s?)\\{\\s?(.*?)\\s?\\}");
//    public static final Pattern arrayRegex = Pattern.compile("([+-]?\\d*\\.\\d*)\\s?([N|E]?)");
    public static final Pattern matrixRegex =
            Pattern.compile("\\[\\s?([+-]?\\d+\\.?\\d+)\\s?([N|E]?)\\s?,\\s?([+-]?\\d+\\.?\\d+)\\s?([N|E]?)\\s?\\]");

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

    // number of km per degree = ~111km (111.32 in google maps, but range varies
    // between 110.567km at the equator and 111.699km at the poles)
    // 1km in degree = 1 / 111.32km = 0.0089
    // 1m in degree = 0.0089 / 1000 = 0.0000089
    public static double mToA = 1 / ((110567.0 + 111699.0) / 2);

    // Earth’s radius, sphere
    public static double EarthRadius = 6378137.0;

    public static class Nuple<X, Y, Z> {
        private final X x;
        private final Y y;
        private final Z z;

        public Nuple(final X x, final Y y, final Z z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }


        public Z getZ() {
            return z;
        }

        public Y getY() {
            return y;
        }

        public X getX() {
            return x;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Nuple)) return false;
            Nuple<?, ?, ?> nuple = (Nuple<?, ?, ?>) o;
            return Objects.equals(getX(), nuple.getX()) &&
                    Objects.equals(getY(), nuple.getY()) &&
                    Objects.equals(getZ(), nuple.getZ());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getX(), getY(), getZ());
        }

        @Override
        public String toString() {
            return "(" + x + y + z + ')';
        }
    }

    public static <X, Y, Z> Nuple<X, Y, Z> nuple(final X x, final Y y, final Z z) {
        return new Nuple<>(x,y,z);
    }
}
