package utils;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static utils.SQL.COMMENT.MARKER_ID;
import static utils.SQL.COMMENT.POSTING_DATE;
import static utils.SQL.MARKER.*;
import static utils.Logging.println;

@SuppressWarnings({"SpellCheckingInspection", "unused", "FieldCanBeLocal"})
public class SQL {

    public enum TABLE{
        MARKERS,
        COMMENTS;

        public String signature() {
            String lowercaseName = this.name().toLowerCase();
            return lowercaseName.replace(
                    lowercaseName.toCharArray()[0],
                    this.name().toCharArray()[0]
            );
        }

    }

    public enum MARKER {
        ID,
        N_DETECTIONS,
        COUNTRY,
        COUNTRY_CODE,
        REGION,
        COUNTY,
        CITY,
        DISTRICT,
        SUBURB,
        TOWN,
        VILLAGE,
        PLACE,
        POSTCODE,
        NEIGHBOURHOOD,
        ROAD,
        HOUSE_NUMBER,
        COORDINATES;

        public String signature() {
            return this.name().toLowerCase();
        }

        public static Stream<MARKER> fields() {
            return Arrays.stream(values());
        }
    }

    public enum COMMENT {
        MARKER_ID,
        TEXT,
        POSTING_DATE;

        public String signature() {
            return this.name().toLowerCase();
        }

        public static Stream<COMMENT> fields() {
            return Arrays.stream(values());
        }
    }

    private static String selectFormat = "SELECT %s FROM %s;";
    private static String selectWhereFormat = "SELECT %s FROM %s WHERE %s;";
    private static String selectWhereLessFormat = "SELECT %s FROM %s WHERE %s < %s;";
    private static String selectWhereMoreFormat = "SELECT %s FROM %s WHERE %s > %s;";
    private static String selectWhereEqualFormat = "SELECT %s FROM %s WHERE %s = %s;";
    private static String selectWhereNotEqualFormat = "SELECT %s FROM %s WHERE %s <> %s;";
    private static String selectWhereBetweenFormat = "SELECT %s FROM %s WHERE %s BETWEEN &s AND %s;";

    private static String updateFormat = "UPDATE %s SET %s;";
    private static String updateFormatWhere = "UPDATE %s SET %s WHERE %s;";
    private static String updateFormatWhereEqual = "UPDATE %s SET %s WHERE %s = %s;";

    private static String insertFormat = "INSERT INTO %s(%s) VALUES(%s);";

    private static String function1PFormat = "%s(%s)";
    private static String function2PFormat = "%s(%s, %s)";
    private static String function3PFormat = "%s(%s, %s, %s)";
    private static String entryFormat = "'%s',%s";
    private static String aliasFormat = "%s AS %s";
    private static String identityFormat = "%s = %s";
    private static String sumFormat = "%s + %s";
    private static String postgresJSONFunctionFormat = "%s(%s)::json->'%s'";

    private static String AddressNodeJsonObj =
            String.format(aliasFormat,
                    json_BuildObject(MARKER.fields().filter(s ->
                        !s.equals(ID) && !s.equals(N_DETECTIONS) &&
                        !s.equals(POSTCODE) && !s.equals(COUNTRY_CODE) &&
                        !s.equals(COORDINATES) && !s.equals(HOUSE_NUMBER)
                    ).map(MARKER::signature)), "addressNode"
            );

    private static String CoordinatesJsonArray = String.format(aliasFormat,
            ST_AsGeoJSON(MARKER.COORDINATES.signature(), MARKER.COORDINATES.signature()), MARKER.COORDINATES.signature()
    );

    private static String selectBody =
            String.join(",", ID.toString(), N_DETECTIONS.toString(), AddressNodeJsonObj, CoordinatesJsonArray);

    private static String json_BuildObject(Stream<String> columns) {
        return String.format(function1PFormat,
                "json_build_object", columns.map(c -> String.format(entryFormat, c, c)).collect(Collectors.joining(", "))
        );
    }

    private static String ST_AsGeoJSON(String geom, String key) {
        return String.format(postgresJSONFunctionFormat, "ST_AsGeoJson", geom, key);
    }

    private static String ST_DistanceSphere(String geom1, String geom2){
        return String.format(function2PFormat, "ST_DistanceSphere", geom1, geom2);
    }

    private static String ST_SetSRID(String geom, Integer SRID){
        return String.format(function2PFormat, "ST_SetSRID", geom, SRID.toString());
    }

    private static String ST_MakeLine(String geom1, String geom2) {
      return String.format(function2PFormat, "ST_MakeLine", geom1, geom2);
    }

    private static String ST_MakePoint(String x, String y){
      return String.format(function2PFormat, "ST_MakePoint", x, y);
    }

    private static String assign(String x, String y) {
        return String.format(identityFormat, x, y);
    }

    private static String sum(String x, String y) {
        return String.format(sumFormat, x, y);
    }

    public static Function<String, String> selectMarkersQuery =
            (filters) -> String.format(selectWhereFormat, selectBody, TABLE.MARKERS.signature(), filters);

    public static String selectMarkerByUId = selectMarkersQuery.apply(
            String.format(identityFormat, MARKER.ID.toString(), ":marker_id")
    );

    public static String selectOnRouteQuery =
            String.format(
                selectWhereLessFormat, selectBody, TABLE.MARKERS.signature(),
                ST_DistanceSphere(
                    ST_SetSRID(
                        ST_MakeLine(ST_MakePoint(":x_A", ":y_A"), ST_MakePoint(":x_B", ":y_B")),
                        4326
                    ),
                    String.join(".", TABLE.MARKERS.signature(), MARKER.COORDINATES.signature())
                ), ":dist");

    public static String selectInAreaQuery =
            String.format(
                selectWhereLessFormat, selectBody, TABLE.MARKERS.signature(),
                ST_DistanceSphere(
                    ST_SetSRID(ST_MakePoint(":x", ":y"), 4326),
                        String.join(".", TABLE.MARKERS.signature(), MARKER.COORDINATES.signature())
                ),":radius"
            );


    public static String insertMarkerQuery =
            String.format(
                insertFormat, TABLE.MARKERS.signature() ,
                    MARKER.fields()
                        .filter(s -> !s.equals(ID) && !s.equals(N_DETECTIONS))
                        .map(MARKER::signature)
                        .collect(Collectors.joining(",")),
                    MARKER.fields()
                        .filter(s -> !s.equals(ID) && !s.equals(N_DETECTIONS))
                        .map(s -> s.equals(COORDINATES) ? ST_SetSRID(ST_MakePoint(":x", ":y"), 4326) : ":"+s.signature())
                        .collect(Collectors.joining(","))
            );

    public static String insertCommentToMarkerQuery =
            String.format(
                insertFormat, TABLE.COMMENTS.signature(),
                    COMMENT.fields()
                        .filter(s -> !s.equals(MARKER_ID) && !s.equals(POSTING_DATE))
                        .map(COMMENT::signature)
                        .collect(Collectors.joining(",")),
                    COMMENT.fields()
                            .filter(s -> !s.equals(MARKER_ID) && !s.equals(POSTING_DATE))
                        .map(s -> ":"+s.signature())
                        .collect(Collectors.joining(","))
            );

    public static String upvoteMarkerQuery =
            String.format(
                updateFormatWhereEqual, TABLE.MARKERS.signature(),
                    assign(
                        MARKER.N_DETECTIONS.signature(),
                        sum(MARKER.N_DETECTIONS.signature(), "1")
                    ),
                    MARKER.ID.signature(), ":marker_id"
            );

    public static void main(String[] args) {
        println("GET RES: " + selectMarkersQuery.apply(Boolean.toString(true)));
        println("GET RES: " + selectMarkerByUId);
        println("GET ROUTE: " + selectOnRouteQuery);
        println("GET AREA: " + selectInAreaQuery);
        println("POST RES: " + insertMarkerQuery);
        println("PUT RES: " + insertCommentToMarkerQuery);
        println("PUT RES: " + upvoteMarkerQuery);

        println(MARKER.fields().collect(Collectors.toList()));
        println(COMMENT.fields().collect(Collectors.toList()));

        println(SQL.AddressNodeJsonObj);
        println(SQL.CoordinatesJsonArray);
    }

}
