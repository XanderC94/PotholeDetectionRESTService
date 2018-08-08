package prototype;

import json.GeoCoordinates;
import json.Segment;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static utils.Utils.*;

public class TryGeographicalOperations {

    public static void main(String[] args) {

        Segment v = new Segment(
                new GeoCoordinates(44.4949, 11.3426),
                new GeoCoordinates(43.9921, 12.6503)
        );

        // NOTE:
        // Latitude (Y) = Distance (°/Rads) from the equator [-Pi/2, Pi/2]
        // Longitude (X) = Distance (°/Rads) from the main meridian [-Pi, Pi]

        // 0. Calculate the mid-point of the segment AB

        GeoCoordinates midPoint = midPoint(v.getA(), v.getB());

        println("MidPoint:" + midPoint.toString());

        // 1. Calculate the length of the segment AB

        double height = 10000 ; // meters
        double distance = haversineDistance(v.getA(), v.getB()) + 2 * height;

        println("AB:" + Double.toString(distance) + "(m)");

        distance *= mToA;
        height *= mToA;

        println("AB:" + Double.toString(distance) + "(deg)");

        // 2. Calculate BB vertices for the given distance, centered in the origin.

        List<GeoCoordinates> boundingBox = Arrays.asList(
                new GeoCoordinates(+ distance / 2, + height),
                new GeoCoordinates(- distance / 2, - height),
                new GeoCoordinates(+ distance / 2, - height),
                new GeoCoordinates(- distance / 2, + height)
        );

        println(boundingBox);

        // 3. Rotate the points to math the angular coefficient of the segment AB. Rodriguez Formula.

        double angularCoefficient =
                (v.getB().getLat() - v.getA().getLat()) /
                        (v.getB().getLng() - v.getA().getLng());

        println("M:" + Double.toString(Math.toDegrees(Math.atan(angularCoefficient))) + "(deg)");

        boundingBox = boundingBox.stream()
                .map(gc -> cartesian2DRotation(gc, Math.atan(angularCoefficient)))
                .collect(Collectors.toList());

        println(boundingBox);

        println("Rotated: " + Double.toString(haversineDistance(boundingBox.get(0), boundingBox.get(3))));
        println("Rotated: " + Double.toString(haversineDistance(boundingBox.get(1), boundingBox.get(2))));

        boundingBox = boundingBox.parallelStream()
                .map(gc ->
                        new GeoCoordinates(
                                midPoint.getLat() + gc.getLat(),
                                midPoint.getLng() + gc.getLng()
                            )
                ).collect(Collectors.toList());


        println("Translated: " + Double.toString(haversineDistance(boundingBox.get(0), boundingBox.get(3))));
        println("Translated: " + Double.toString(haversineDistance(boundingBox.get(1), boundingBox.get(2))));

        println(boundingBox);
    }

}
