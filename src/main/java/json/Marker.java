package json;

import java.util.Objects;

public class Marker {

    private long ID = 0;
    private long nDetections = 0;
    private GeoCoordinates coordinates;
    private OSMAddressNode addressNode;

    public Marker(){}

    public Marker(final long mid, final long n_detections, final GeoCoordinates coordinates, final OSMAddressNode addressNode){

        this.ID = mid;
        this.nDetections = n_detections;
        this.coordinates = coordinates;
        this.addressNode = addressNode;
    }

    public Marker(final long n_detections, final GeoCoordinates coordinates, final OSMAddressNode addressNode){

        this.nDetections = n_detections;
        this.coordinates = coordinates;
        this.addressNode = addressNode;
    }

    public Marker(final GeoCoordinates coordinates, final OSMAddressNode addressNode){

        this.coordinates = coordinates;
        this.addressNode = addressNode;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public void setnDetections(long nDetections) {
        this.nDetections = nDetections;
    }

    public void setCoordinates(GeoCoordinates coordinates) {
        this.coordinates = coordinates;
    }

    public void setAddressNode(OSMAddressNode addressNode) {
        this.addressNode = addressNode;
    }

    public long getID() {
        return ID;
    }

    public long getnDetections() {
        return nDetections;
    }

    public GeoCoordinates getCoordinates() {
        return coordinates;
    }

    public OSMAddressNode getAddressNode() {
        return addressNode;
    }

    @Override
    public String toString() {
        return "Marker{" +
                "ID=" + ID +
                ", nDetections=" + nDetections +
                ", coordinatesTuple=" + coordinates +
                ", addressNode=" + addressNode.toString() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Marker)) return false;
        Marker marker = (Marker) o;
        return Objects.equals(getCoordinates(), marker.getCoordinates()) &&
                Objects.equals(getAddressNode(), marker.getAddressNode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCoordinates(), getAddressNode());
    }
}
