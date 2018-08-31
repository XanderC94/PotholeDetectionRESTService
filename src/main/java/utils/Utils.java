package utils;

import json.GeoCoordinates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {

    final static Logger logger = LoggerFactory.getLogger("REST-Logger");

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

    public static void log(String s) {
        logger.info(s);
    }

    public static <T extends Object> void log(Collection<T> x) {
        final String str = String.join(",",
                x.stream().map(Object::toString).collect(Collectors.toList())
        );

        logger.info(str);
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

    public static <X, Y, Z> Nuple<X, Y, Z> nuple(final X x, final Y y, final Z z) {
        return new Nuple<>(x,y,z);
    }

    public enum FORMAT implements Function<Double[], GeoCoordinates> {
        LAT_LNG((a, b) -> new GeoCoordinates(b, a)),
        LNG_LAT(GeoCoordinates::new);

        private final BiFunction<Double, Double, GeoCoordinates> formatter;

        FORMAT(final BiFunction<Double, Double, GeoCoordinates> formatter) {
            this.formatter = formatter;
        }

        @Override
        public GeoCoordinates apply(final Double[] coordinates) {
            return this.formatter.apply(coordinates[0], coordinates[1]);
        }
    }

    public enum CHECK_CODE implements Function<String, String> {

        OK("Format %s is Correct"),
        DUPLICATED("Duplicated %s"),
        BAD_FORMAT("Coordinates must be like [x.y {N|E|}, w.z {E|N|}], instead got %s");

        private String value;

        CHECK_CODE(final String value) {
            this.value = value;
        }

        @Override
        public String apply(String s) {
            return String.format(this.value, s);
        }
    }

    /**
     * Check the given string representation of N-E coordinates
     *
     * @param gc
     * @return formatted coordinates, info and found format
     */
    public static Tuple<Optional<GeoCoordinates>, String> checkCoordinatesFormat(final String gc) {
        Matcher m = matrixRegex.matcher(gc.trim().toUpperCase());
        Optional<GeoCoordinates> coordinates;
        String code;

        if (m.find()) {

            final Double
                    a = Double.valueOf(m.group(1)),
                    b = Double.valueOf(m.group(3));

            final Boolean
                    aIsLat = m.group(2).equals("N"),
                    aIsLng = m.group(2).equals("E"),
                    bIsLat = m.group(4).equals("N"),
                    bIsLng = m.group(4).equals("E"),
                    isStandard = m.group(2).equals("") && m.group(4).equals("");

            if (aIsLat && bIsLat || aIsLng && bIsLng) {
                // Check Duplicated if N|E are enforced
                coordinates = Optional.empty();
                code = CHECK_CODE.DUPLICATED.apply(m.group(2));

            } else {

                if (aIsLng && bIsLat || isStandard) { // standard LNG_LAT
                    coordinates = Optional.of(new GeoCoordinates(a, b));
                } else { // enforced LAT_LNG => saved as LNG_LAT
                    coordinates =  Optional.of(new GeoCoordinates(b, a));
                }

                code = CHECK_CODE.OK.apply(coordinates.toString());
            }

            return new Tuple<>(coordinates, code);

        } else {
            return new Tuple<>(Optional.empty(), CHECK_CODE.BAD_FORMAT.apply(gc));
        }
    }
}
