package json;

import utils.Utils;

import java.util.List;

public class Geometry {

    private String type;
    private List<GeoCoordinates> vertices;

    public Geometry(final String type, final List<GeoCoordinates> vertices) {
        this.type = type;
        this.vertices = vertices;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<GeoCoordinates> getVertices() {
        return vertices;
    }

    public void setVertices(final List<GeoCoordinates> vertices) {
        this.vertices = vertices;
    }

    @Override
    public String toString() {
        return "Geometry{" +
                "type='" + type + '\'' +
                ", vertices=" + Utils.collectionToString(vertices) +
                '}';
    }
}
