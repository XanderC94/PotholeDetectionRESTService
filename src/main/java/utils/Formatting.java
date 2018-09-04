package utils;

import json.GeoCoordinates;
import json.Tuple;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;

import static utils.Regex.coordinatesTuple;
import static utils.Factory.*;

public class Formatting {
    /**
     * Check the given string representation of N-E coordinatesTuple
     *
     * @param gc
     * @return formatted coordinatesTuple, info and found format
     */
    public static Tuple<Optional<GeoCoordinates>, String> checkCoordinatesFormat(final String gc) {
        Matcher m = coordinatesTuple.matcher(gc.trim().toUpperCase());
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

            return tuple(coordinates, code);

        } else {
            return tuple(Optional.empty(), CHECK_CODE.BAD_FORMAT.apply(gc));
        }
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
}
