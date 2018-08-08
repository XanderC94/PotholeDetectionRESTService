package json;

public class Segment {

    private GeoCoordinates a;
    private GeoCoordinates b;


    public Segment(GeoCoordinates a, GeoCoordinates b) {
        this.a = a;
        this.b = b;
    }

    public GeoCoordinates getA() {
        return a;
    }

    public void setA(GeoCoordinates a) {
        this.a = a;
    }

    public GeoCoordinates getB() {
        return b;
    }

    public void setB(GeoCoordinates b) {
        this.b = b;
    }

    @Override
    public String toString() {
        return "Segment{" +
                "a=" + a.toString() +
                ", b=" + b.toString() +
                '}';
    }
}
