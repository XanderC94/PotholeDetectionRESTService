package utils;

import json.GeoCoordinates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static utils.Regex.coordinatesTuple;

public class Utils {

    final static Logger logger = LoggerFactory.getLogger("REST-Logger");

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

    public static NameFilter provincesFilter = new ProvinceFilter("src/main/resources/iso/provinces.csv");

    public static NameFilter regionFilter = new RegionFilter("src/main/resources/iso/states.csv");

    public static String clean(String str) {
        return str
                .replace("`","")
                .replace("´", "")
                .replace("\'", "")
                .replace("-","_")
                .replace(" ", "_")
                .toUpperCase();

    }

    // number of km per degree = ~111km (111.32 in google maps, but range varies
    // between 110.567km at the equator and 111.699km at the poles)
    // 1km in degree = 1 / 111.32km = 0.0089
    // 1m in degree = 0.0089 / 1000 = 0.0000089
    public static double mToA = 1 / ((110567.0 + 111699.0) / 2);

    // Earth’s radius, sphere
    public static double EarthRadius = 6378137.0;

}
