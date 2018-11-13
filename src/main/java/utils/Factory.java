package utils;

import json.*;

public class Factory {

    public static <X, Y, Z> Tuple3<X, Y, Z> nuple(final X x, final Y y, final Z z) {

        return new Tuple3<>(x,y,z);
    }

    public static <X, Y, W, Z> Tuple4<X, Y, W, Z> quple(final X x, final Y y, final W w, final Z z) {

        return new Tuple4<>(x,y,w,z);
    }

    public static <X, Y> Tuple<X, Y> tuple(final X x, final Y y) {

        return new Tuple<>(x,y);
    }

    public static GeoCoordinates geoCoordinates(final Double x, final Double y) {

        return new GeoCoordinates(x,y);
    }

    public static Marker marker(final long mid, final long n_detections,
                                final GeoCoordinates coordinates,
                                final OSMAddressNode addressNode){

        return new Marker(mid, n_detections, coordinates, addressNode);
    }

    public static Marker marker(final long n_detections, final GeoCoordinates coordinates, final OSMAddressNode addressNode){

        return new Marker(n_detections, coordinates, addressNode);
    }

    public static Marker marker(final GeoCoordinates coordinates, final OSMAddressNode addressNode){

        return new Marker(coordinates, addressNode);
    }


}
