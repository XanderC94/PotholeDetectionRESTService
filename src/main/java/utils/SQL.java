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
            (geom1, geom2) -> "ST_DistanceSphere(" + geom1 + "," + geom2 + ")";

    private static BiFunction<String, Integer, String> ST_SetSRID =
            (geom, SRID) -> "ST_SetSRID(" + geom + "," + SRID.toString() + ")";

    private static BiFunction<String, String, String> ST_MakeLine =
            (geom1, geom2) -> "ST_MakeLine(" + geom1 + "," + geom2 + ")";

    private static BiFunction<String, String, String> ST_MakePoint =
            (x, y) -> "ST_MakePoint(" + x +"," + y + ")";

    public static Function<String, String> getResourceQuery =
            (filters) -> String.join(" ", "SELECT", selectBody, "FROM", TABLE.MARKERS.table_name, filters) + ";";

    public static String getRouteQuery =
            String.join(" ",
                    "SELECT", selectBody, "FROM", TABLE.MARKERS.table_name, "WHERE",
                        ST_DistanceSphere.apply(
                                ST_SetSRID.apply(
                                        ST_MakeLine.apply(
                                                ST_MakePoint.apply(":x_A", ":y_A"),
                                                ST_MakePoint.apply(":x_B", ":y_B")
                                        ),
                                        4326
                                ),
                                String.join(".", TABLE.MARKERS.table_name, "coordinates")
                        ), "< :dist;");

    public static String getAreaQuery =
            String.join(" ",
                    "SELECT", selectBody, "FROM", TABLE.MARKERS.table_name, "WHERE",
                        ST_DistanceSphere.apply(
                                ST_SetSRID.apply(ST_MakePoint.apply(":x", ":y"), 4326),
                                String.join(".", TABLE.MARKERS.table_name, "coordinates")
                        ),"< :radius;"
            );


    public static String addMarkerQuery =
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

    public static String addCommentQuery =
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
        Utils.println("GET RES: " + getResourceQuery.apply(""));
        Utils.println("GET ROUTE: " + getRouteQuery);
        Utils.println("GET AREA: " + getAreaQuery);
        Utils.println("POST RES: " +addMarkerQuery);
        Utils.println("PUT RES: " +addCommentQuery);
    }

}
