package json;

import utils.Utils;

import java.util.Objects;
import java.util.regex.Matcher;

import static utils.Utils.matrixRegex;

public class GeoCoordinates {
    private double lat;
    private double lng;
    private double radius = Utils.EarthRadius;

    public GeoCoordinates(final double lat, final double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    @Override
    public String toString() {
        return "[" +
                this.getLat() + " N, " +
                this.getLng() + "E" +
                "]";
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeoCoordinates)) return false;
        GeoCoordinates that = (GeoCoordinates) o;
        return Double.compare(that.getLat(), getLat()) == 0 &&
                Double.compare(that.getLng(), getLng()) == 0 &&
                Double.compare(that.getRadius(), getRadius()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLat(), getLng(), getRadius());
    }

    public static GeoCoordinates fromString(final String gc) throws Exception {

        GeoCoordinates coordinates = new GeoCoordinates(0,0);

        Matcher m = matrixRegex.matcher(gc.trim().toUpperCase());

        if (m.find()) {

            if (m.group(2).equals("E") && m.group(4).equals("E") ||
                    m.group(2).equals("N") && m.group(4).equals("N")) {
                throw new Exception("Duplicated " + m.group(2));
            } else {
                if (m.group(2).equals("E")) {
                    coordinates.setLng(Double.valueOf(m.group(1)));
                    coordinates.setLat(Double.valueOf(m.group(3)));
                } else {
                    coordinates.setLat(Double.valueOf(m.group(1)));
                    coordinates.setLng(Double.valueOf(m.group(3)));
                }
            }
        } else {
            throw new Exception("Coordinates must be like [x.y {N|E|}, w.z {E|N|}] ");
        }

        return coordinates;
    }

    public static GeoCoordinates empty() {
        return new GeoCoordinates(0.0, 0.0);
    }
}
