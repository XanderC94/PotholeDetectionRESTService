package json;

import utils.Utils;

import java.util.Objects;

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
                lat + " N" + ", " +
                lng + " E" +
                '}';
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
}
