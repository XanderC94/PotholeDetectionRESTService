package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import json.GeoCoordinates;
import json.Marker;
import json.OSMAddressNode;
import json.Tuple4;
import org.jdbi.v3.core.statement.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static utils.Factory.tuple4;
import static utils.Regex.coordinatesTuple;

public class Utils {

    public static final Gson gson = new GsonBuilder().create();

    public static final String geoCodingURLFormat =
            "https://nominatim.openstreetmap.org/search?q=%s&format=%s&polygon_geojson=1&limit=%d";
    public static final String reverseGeoCodingURLFormat =
            "https://nominatim.openstreetmap.org/reverse?lon=%.9f&lat=%.9f&format=%s";

    public static final String openRoutingServiceURLFormat =
            "https://api.openrouteservice.org/directions?" +
                    "api_key=%s&coordinates=%.9f,%.9f|%.9f,%.9f&profile=%s&preference=%s&geometry_format=%s";


    public static final String ORS_API_KEY = "5b3ce3597851110001cf6248c396a2051da84b2ea36fa8e7f8f99d89";

    public static NameFilter provincesFilter = new ProvinceFilter("src/main/resources/iso/provinces.csv");

    public static NameFilter regionFilter = new RegionFilter("src/main/resources/iso/states.csv");

    public static final String defaultTown = "none";
    public static final String defaultCounty = "none";
    public static final String defaultCountry = "none";
    public static final String defaultRegion = "none";
    public static final String defaultRoad = "none";

    public static String clean(String str) {
        return str
                .replace("`","")
                .replace("´", "")
                .replace("\'", "")
                .replace("-","_")
                .replace(" ", "_")
                .toUpperCase();

    }

    public static String stringify(final String str) { return "'" + str + "'";}

    public static <T extends Object> String collectionToString(Collection<T> x) {
        return String.join(",",
                x.stream().map(Object::toString).collect(Collectors.toList())
        );
    }

    public static String createFilter(final String country, final String region,
                                      final String county, final String town,
                                      final String road) {

        final Map<String, Boolean> enabledFilters = new HashMap<>();
        final Map<String, Boolean> specialFilters = new HashMap<>();

        enabledFilters.put("country", !country.toLowerCase().equals(defaultCountry));
        enabledFilters.put("region", !region.toLowerCase().equals(defaultRegion));
        enabledFilters.put("county", !county.toLowerCase().equals(defaultCounty));
        enabledFilters.put("road", !road.toLowerCase().equals(defaultRoad));

        specialFilters.put("town", !town.toLowerCase().equals(defaultTown));
        specialFilters.put("city", !town.toLowerCase().equals(defaultTown));

        final List<String> eFilters = enabledFilters.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(e-> e.getKey() + " ILIKE :" + e.getKey().toLowerCase()+"")
                .collect(Collectors.toList());

        final List<String> sFilters = specialFilters.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(e-> e.getKey() + " ILIKE :" + e.getKey().toLowerCase()+"")
                .collect(Collectors.toList());

        final String eFilter = String.join(" AND ", eFilters);
        final String sFilter = String.join(" OR ", sFilters) ;

        final String filter = String.join(" AND ", eFilter, "(" + sFilter + ")");

//        println(filter);

        return eFilters.isEmpty() && sFilters.isEmpty() ? Boolean.toString(true) :
                eFilters.isEmpty() ? sFilter :
                        sFilter.isEmpty() ? eFilter :
                                filter;
    }

    public static List<Marker> resolveQuery(final Query q) throws Exception{
        List<Tuple4<Integer, Integer, String, String>> resultSets =
                q.map((rs, ctx) -> tuple4(
                        rs.getInt("ID"),
                        rs.getInt("N_DETECTIONS"),
                        rs.getString("coordinates").replace("\'", ""),
                        rs.getString("addressNode").replace("\'", "")
                )).list();

        List<Marker> res = new ArrayList<>();

//        println(resultSets);

        for (Tuple4<Integer, Integer, String, String> n : resultSets) {
            res.add(new Marker(
                    Long.valueOf(n.getX()),
                    Long.valueOf(n.getY()),
                    GeoCoordinates.fromString(n.getW()),
                    gson.fromJson(n.getZ(), OSMAddressNode.class).unfiltered()
            ));
        }

//        Utils.println(res);

        return res;
    }

    // number of km per degree = ~111km (111.32 in google maps, but range varies
    // between 110.567km at the equator and 111.699km at the poles)
    // 1km in degree = 1 / 111.32km = 0.0089
    // 1m in degree = 0.0089 / 1000 = 0.0000089
    public static double mToA = 1 / ((110567.0 + 111699.0) / 2);

    // Earth’s radius, sphere
    public static double EarthRadius = 6378137.0;

    // a variation of 0.0000089 degrees corresponds approximately to 1m variation
    public static final Double minimumDegreesVariation = 0.0000089;
    public static final Double minimumMetersVariation = 1.0;

}
