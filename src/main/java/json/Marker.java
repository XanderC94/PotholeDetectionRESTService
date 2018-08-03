package json;

public class Marker {

    private long MID = 0;
    private long nDetections = 0;
    private GeoCoordinates coordinates;
    private OSMAddressNode addressNode;

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

    public void setMID(long MID) {
        this.MID = MID;
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
        return "Marker{" +
                "MID=" + MID +
                ", nDetections=" + nDetections +
                ", coordinates=" + coordinates +
                ", addressNode=" + addressNode.toString() +
                '}';
    }
}
