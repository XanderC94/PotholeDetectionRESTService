package prototype;

import json.GeoCoordinates;
import json.Segment;
import utils.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TryGeographicalOperations {

    public static void main(String[] args) {

        Segment v = new Segment(
                new GeoCoordinates(44.4949, 11.3426),
                new GeoCoordinates(43.9921, 12.6503)
        );

        // NOTE: Latitude (X) and Longitude (Y) are the angles of in degrees
        // of a point on the sphere surface from the Origin.

        // 0. Calculate the mid-point of the segment AB

        GeoCoordinates midPoint = Utils.midPoint(v.getA(), v.getB());

        Utils.println("MidPoint:" + midPoint.toString());

        // 1. Calculate the length of the segment AB

        double height = 1000 * Utils.mToA; // meters
        double distance = Utils.haversineDistance(v.getA(), v.getB()) * Utils.mToA + height;

        Utils.println("AB:" + Double.toString(distance) + "(deg)");

        // 2. Calculate BB vertices for the given distance, using the modPoint as origin.

        List<GeoCoordinates> boundingBox = Arrays.asList(
                new GeoCoordinates(midPoint.getLat() + distance / 2, midPoint.getLng() + height / 2),
                new GeoCoordinates(midPoint.getLat() - distance / 2, midPoint.getLng() - height / 2),
                new GeoCoordinates(midPoint.getLat() + distance / 2, midPoint.getLng() - height / 2),
                new GeoCoordinates(midPoint.getLat() - distance / 2, midPoint.getLng() + height / 2)
        );

        Utils.println(boundingBox);

        // 3. Rotate the points to math the angular coefficient of the segment AB. Rodriguez Formula.

        double angularCoefficient =
                (v.getA().getLng() - v.getB().getLng()) /
                        (v.getA().getLat() - v.getB().getLat());

        Utils.println("M:" + Double.toString(Math.toDegrees(Math.atan(angularCoefficient))) + "(deg)");

        boundingBox = boundingBox.parallelStream()
                .map(gc ->
                        Utils.rodriguezRotation(gc, midPoint, Math.toDegrees(Math.atan(angularCoefficient)))
                ).collect(Collectors.toList());

        Utils.println(boundingBox);
    }

}
