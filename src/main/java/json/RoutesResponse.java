package json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.List;

/**
 * Created by Matteo Gabellini on 30/08/2018.
 */
public class RoutesResponse {
    private List<Marker> markers;
    private JsonNode routeServiceResponse;



    public RoutesResponse(List<Marker> markers, JsonNode routeServiceResponse){
        this.markers = markers;
        this.routeServiceResponse = routeServiceResponse;
    }

    public RoutesResponse(List<Marker> markers, String routeServiceResponse) throws IOException{
        this.markers = markers;
        JsonParser parser = new JsonParser();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        JsonNode jsonResponse = mapper.readTree(routeServiceResponse);//parser.parse(routeServiceResponse);
        this.routeServiceResponse = jsonResponse;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "{" +
                "markers: " + markers.toString() + "," +
                "routeServiceResponse" + routeServiceResponse.toString() +
                " }";
    }

    public List<Marker> getMarkers() {
        return markers;
    }

    public void setMarkers(List<Marker> markers) {
        this.markers = markers;
    }

    public JsonNode getRouteServiceResponse() {
        return routeServiceResponse;
    }

    public void setRouteServiceResponse(JsonNode routeServiceResponse) {
        this.routeServiceResponse = routeServiceResponse;
    }

    public void setRouteServiceResponse(String routeServiceResponse) throws IOException{
        JsonParser parser = new JsonParser();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure( SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        JsonNode jsonResponse = null;//pars
        jsonResponse = mapper.readTree(routeServiceResponse);
        this.routeServiceResponse = jsonResponse;
    }
}
