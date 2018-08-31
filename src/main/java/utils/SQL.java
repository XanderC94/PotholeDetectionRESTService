package utils;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("SpellCheckingInspection")
public class SQL {

    private enum TABLE {
        MARKERS("Markers",
                Arrays.asList(
                        "id",
                        "n_detections",
                        "country",
                        "country_code",
                        "region",
                        "county",
                        "city",
                        "district",
                        "suburb",
                        "town",
                        "village",
                        "place",
                        "postcode",
                        "neighbourhood",
                        "road",
                        "house_number",
                        "coordinates"
                )
        ),
        COMMENTS("Comments",
                Arrays.asList(
                        "marker_id",
                        "text",
                        "posting_date"
                )
        );

        private final String table_name;
        private final List<String> columns;

        TABLE(final String table_name, final List<String> columns) {

            this.table_name = table_name;
            this.columns = columns;
        }
    }

    private static String selectWhereFormat = "SELECT %s FROM %s WHERE %s;";
    private static String selectWhereLessFormat = "SELECT %s FROM %s WHERE %s < %s;";
    private static String selectWhereMoreFormat = "SELECT %s FROM %s WHERE %s > %s;";
    private static String selectWhereEqualFormat = "SELECT %s FROM %s WHERE %s = %s;";
    private static String selectWhereNotEqualFormat = "SELECT %s FROM %s WHERE %s <> %s;";
    private static String selectWhereBetweenFormat = "SELECT %s FROM %s WHERE %s BETWEEN &s AND %s;";

    private static String selectFormat = "SELECT %s FROM %s;";

    private static String insertFormat = "INSERT (%s) INTO %s VALUES(%s);";

    private static String function1PFormat = "%s(%s)";
    private static String function2PFormat = "%s(%s, %s)";
    private static String function3PFormat = "%s(%s, %s, %s)";

    private static String ID = "id";

    private static String AddressNodeJsonObj =
            "json_build_object(" +
                "'country',country," +
                "'region',region," +
                "'county',county," +
                "'city',city," +
                "'district',district," +
                "'suburb',suburb," +
                "'town',town," +
                "'village',village," +
                "'place',place," +
                "'neighbourhood',neighbourhood," +
                "'road',road"+
            ") AS addressNode";

    private static String CoordinatesJsonArray = "ST_AsGeoJSON(coordinates)::json->'coordinates' AS coordinates";

    private static String selectBody = String.join(",", ID, AddressNodeJsonObj, CoordinatesJsonArray);

    private static BiFunction<String, String, String> ST_DistanceSphere =
            (geom1, geom2) -> String.format(function2PFormat, "ST_DistanceSphere", geom1, geom2);

    private static BiFunction<String, Integer, String> ST_SetSRID =
            (geom, SRID) -> String.format(function2PFormat, "ST_SetSRID", geom, SRID.toString());

    private static BiFunction<String, String, String> ST_MakeLine =
            (geom1, geom2) -> String.format(function2PFormat, "ST_MakeLine", geom1, geom2);

    private static BiFunction<String, String, String> ST_MakePoint =
            (x, y) -> String.format(function2PFormat, "ST_MakePoint", x, y);

    public static Function<String, String> selectMarkersQuery =
            (filters) -> String.join(" ", "SELECT", selectBody, "FROM", TABLE.MARKERS.table_name, filters) + ";";

    public static String selectOnRouteQuery =
            String.format(selectWhereLessFormat, selectBody, TABLE.MARKERS.table_name,
                    ST_DistanceSphere.apply(
                        ST_SetSRID.apply(
                            ST_MakeLine.apply(
                                ST_MakePoint.apply(":x_A", ":y_A"),
                                ST_MakePoint.apply(":x_B", ":y_B")
                            ),
                            4326
                        ),
                        String.join(".", TABLE.MARKERS.table_name, "coordinates")
                    ), ":dist");

//            String.join(" ",
//                    "SELECT", selectBody, "FROM", TABLE.MARKERS.table_name, "WHERE",
//                        , "< ;");

    public static String selectInAreaQuery =
            String.join(" ",
                    "SELECT", selectBody, "FROM", TABLE.MARKERS.table_name, "WHERE",
                        ST_DistanceSphere.apply(
                                ST_SetSRID.apply(ST_MakePoint.apply(":x", ":y"), 4326),
                                String.join(".", TABLE.MARKERS.table_name, "coordinates")
                        ),"< :radius;"
            );


    public static String insertMarkerQuery =
            String.join(" ",
                    "INSERT", "INTO", TABLE.MARKERS.table_name , "(",
                        TABLE.MARKERS.columns.stream()
                            .filter(s -> !s.equals("id") && !s.equals("n_detections"))
                            .collect(Collectors.joining(",")),
                    ")",
                    "VALUES", "(",
                        TABLE.MARKERS.columns.stream()
                            .filter(s -> !s.equals("id") && !s.equals("n_detections"))
                            .map(s -> s.equals("coordinates") ? ST_SetSRID.apply(ST_MakePoint.apply(":x", ":y"), 4326) : ":"+s)
                            .collect(Collectors.joining(",")),
                    ");"
            );

    public static String insertCommentToMarkerQuery =
            String.join(" ", "INSERT", "INTO", TABLE.COMMENTS.table_name ,"(",
                    TABLE.COMMENTS.columns.stream()
                            .filter(s -> !s.equals("marker_id") && !s.equals("posting_date"))
                            .collect(Collectors.joining(","))
                    ,")", "VALUES", "(",
                    TABLE.COMMENTS.columns.stream()
                            .filter(s -> !s.equals("marker_id") && !s.equals("posting_date"))
                            .map(s -> ":"+s)
                            .collect(Collectors.joining(","))
                    ,");");

    public static void main(String[] args) {
        Utils.println("GET RES: " + selectMarkersQuery.apply(""));
        Utils.println("GET ROUTE: " + selectOnRouteQuery);
        Utils.println("GET AREA: " + selectInAreaQuery);
        Utils.println("POST RES: " + insertMarkerQuery);
        Utils.println("PUT RES: " + insertCommentToMarkerQuery);
    }

}
