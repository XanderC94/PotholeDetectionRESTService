package json;

public class GeoCoordinates {
    public final double lat;
    public final double lng;

    public GeoCoordinates(final double lat, final double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    public String toString() {
        return "[LAT:"+lat+", LNG:"+lng+"]";
    }
}
