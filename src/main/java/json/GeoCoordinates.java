package json;

import utils.Utils;

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
        return "GeoCoordinates{" +
                "lat=" + lat +
                ", lng=" + lng +
                '}';
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}
