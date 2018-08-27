package utils;

import json.GeoCoordinates;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {

    public static final Pattern addressRegex = Pattern.compile("(?:\"address\":)\\{(.*?)\\}");
    public static final Pattern coordinatesRegex = Pattern.compile("\"lat\":\"[+-]?\\d*\\.\\d*\",\"lon\":\"[+-]?\\d*\\.\\d*\"");
    public static final Pattern routingRegex = Pattern.compile("(?:\"geometry\":\\s?)\\{\\s?(.*?)\\s?\\}");
    public static final Pattern arrayRegex = Pattern.compile("([+-]?\\d*\\.\\d*)\\s?([N|E]?)");
    public static final Pattern matrixRegex =
            Pattern.compile("\\[\\s?([+-]?\\d*\\.\\d*)\\s?([N|E]?)\\s?,\\s?([+-]?\\d*\\.\\d*)\\s?([N|E]?)\\s?\\]");

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

    /**
     * This uses the ‘haversineDistance’ formula to calculate the great-circle
     * distance between two points – that is, the shortest distance over the earth’s surface –
     * giving an ‘as-the-crow-flies’ distance between the points
     * (ignoring any hills they fly over, of course!) in Meters.
     *
     * @param A
     * @param B
     * @return The distance between A and B
     */
    public static double haversineDistance(final GeoCoordinates A, final GeoCoordinates B) {

        double dLat = Math.toRadians(B.getLat() - A.getLat());
        double dLng = Math.toRadians(B.getLng() - A.getLng());

        GeoCoordinates rA = new GeoCoordinates(Math.toRadians(A.getLat()), Math.toRadians(A.getLng()));
        GeoCoordinates rB = new GeoCoordinates(Math.toRadians(B.getLat()), Math.toRadians(B.getLng()));

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(rA.getLat()) * Math.cos(rB.getLat()) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

        return EarthRadius * c;
    }

    public static GeoCoordinates cartesian2DRotation(final GeoCoordinates A, final double theta) {

        return new GeoCoordinates(
                A.getLat()*Math.sin(theta) + A.getLng()*Math.cos(theta), // y
                A.getLat()*Math.cos(theta) - A.getLng()*Math.sin(theta) // x
        );
    }

    public static GeoCoordinates rodriguezRotation(final GeoCoordinates A, final GeoCoordinates O, final double theta) {

        double[] v = {
                Math.sin(A.getLat())*Math.cos(A.getLng()),
                Math.sin(A.getLat())*Math.sin(A.getLng()),
                Math.cos(A.getLat()),
        };

        double[] k = {
                Math.sin(O.getLat())*Math.cos(O.getLng()),
                Math.sin(O.getLat())*Math.sin(O.getLng()),
                Math.cos(O.getLat())
        };

        double[][] K = {
                { 0,                   -k[2]*Math.sin(theta), k[1]*Math.sin(theta)},
                { k[2]*Math.sin(theta), 0,                   -k[0]*Math.sin(theta)},
                {-k[1]*Math.sin(theta), k[0]*Math.sin(theta), 0                   }
        };

        double[][] K2 = {
            {0,                               k[2]*k[2]*(1 - Math.cos(theta)), k[1]*k[1]*(1 - Math.cos(theta))},
            {k[2]*k[2]*(1 - Math.cos(theta)), 0,                               k[0]*k[0]*(1 - Math.cos(theta))},
            {k[1]*k[1]*(1 - Math.cos(theta)), k[0]*k[0]*(1 - Math.cos(theta)), 0                              }
        };

        double[][] R = {
                {1,                K[0][1]*K2[0][1], K[0][2]*K2[0][2]},
                {K[1][0]*K2[1][0], 1,                K[1][2]*K2[1][2]},
                {K[2][0]*K2[2][0], K[2][1]*K2[2][1], 1               }
        };

        double[] b = {
               v[0]*(R[0][0] + R[0][1] + R[0][2]),
               v[1]*(R[1][0] + R[1][1] + R[1][2]),
               v[2]*(R[2][0] + R[2][1] + R[2][2])
        };

        double tanLat = b[0] / b[1];
        double tanLng = Math.sqrt(b[0]*b[0] + b[1]*b[1]) / b[2];

        return new GeoCoordinates(Math.toDegrees(Math.atan(tanLat)), Math.toDegrees(Math.atan(tanLng)));

    }

    public static GeoCoordinates midPoint(final GeoCoordinates A, final GeoCoordinates B){

        double dLng = Math.toRadians(B.getLng() - A.getLng());

        //convert to radians
        GeoCoordinates rA = new GeoCoordinates(Math.toRadians(A.getLat()), Math.toRadians(A.getLng()));
        GeoCoordinates rB = new GeoCoordinates(Math.toRadians(B.getLat()), Math.toRadians(B.getLng()));

        double Mx = Math.cos(rB.getLat()) * Math.cos(dLng);
        double My = Math.cos(rB.getLat()) * Math.sin(dLng);
        double mLat = Math.atan2(Math.sin(rA.getLat()) + Math.sin(rB.getLat()), Math.sqrt((Math.cos(rA.getLat()) + Mx) * (Math.cos(rA.getLat()) + Mx) + My * My));
        double mLng = rA.getLng() + Math.atan2(My, Math.cos(rA.getLat()) + Mx);

        return new GeoCoordinates(Math.toDegrees(mLat), Math.toDegrees(mLng));
    }
}
