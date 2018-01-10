package json;

public class Point {
    public double lat;
    public double lng;

    public Point(final double lat, final double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public Point(final String json_array) {
        String[] temp = json_array.trim()
                .replace("[", "")
                .replace("]", "")
                .split(",");

        this.lat = Double.valueOf(temp[0]);
        this.lng = Double.valueOf(temp[1]);
    }

    @Override
    public String toString() {
        return "[LAT:"+lat+", LNG:"+lng+"]";
    }
}
