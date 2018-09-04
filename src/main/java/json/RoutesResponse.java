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
    private JsonNode routingServiceResponse;



    public RoutesResponse(List<Marker> markers, JsonNode routingServiceResponse){
        this.markers = markers;
        this.routingServiceResponse = routingServiceResponse;
    }

    public RoutesResponse(List<Marker> markers, String routingServiceResponse) throws IOException{
        this.markers = markers;
        JsonParser parser = new JsonParser();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        JsonNode jsonResponse = mapper.readTree(routingServiceResponse);//parser.parse(routingServiceResponse);
        this.routingServiceResponse = jsonResponse;
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
                "routingServiceResponse" + routingServiceResponse.toString() +
                " }";
    }

    public List<Marker> getMarkers() {
        return markers;
    }

    public void setMarkers(List<Marker> markers) {
        this.markers = markers;
    }

    public JsonNode getRoutingServiceResponse() {
        return routingServiceResponse;
    }

    public void setRoutingServiceResponse(JsonNode routingServiceResponse) {
        this.routingServiceResponse = routingServiceResponse;
    }

    public void setRouteServiceResponse(String routingServiceResponse) throws IOException{
        JsonParser parser = new JsonParser();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure( SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        JsonNode jsonResponse = null;//pars
        jsonResponse = mapper.readTree(routingServiceResponse);
        this.routingServiceResponse = jsonResponse;
    }
}
