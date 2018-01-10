package json;

import json.Point;

public class Marker {

    public long MID = 0;
    public long nDetections = 0;
    public Point coordinates;
    public String locationDetection;

    public Marker(){}

    public Marker(final long mid, final long n_detections, final Point coordinates, final String locationDetection){

        this.MID = mid;
        this.nDetections = n_detections;
        this.coordinates = coordinates;
        this.locationDetection = locationDetection;
    }

    public Marker(final long n_detections, final Point coordinates, final String locationDetection){

        this.nDetections = n_detections;
        this.coordinates = coordinates;
        this.locationDetection = locationDetection;
    }

    public Marker(final Point coordinates, final String locationDetection){

        this.coordinates = coordinates;
        this.locationDetection = locationDetection;
    }

    public long getMID() {
        return MID;
    }

    public long getnDetections() {
        return nDetections;
    }

    public Point getCoordinates() {
        return coordinates;
    }

    public String getLocationDetection() {
        return locationDetection;
    }

    @Override
    public String toString() {
        return this.locationDetection +  " --- " + this.coordinates.toString();
    }
}
