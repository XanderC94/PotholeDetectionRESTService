package json;

public class Marker {

    public long MID = 0;
    public long nDetections = 0;
    public GeoCoordinates coordinates;
    public OSMAddressNode addressNode;

    public Marker(){}

    public Marker(final long mid, final long n_detections, final GeoCoordinates coordinates, final OSMAddressNode addressNode){

        this.MID = mid;
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

    public long getMID() {
        return MID;
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
        return this.addressNode.toString() +  " @ " + this.coordinates.toString();
    }
}
