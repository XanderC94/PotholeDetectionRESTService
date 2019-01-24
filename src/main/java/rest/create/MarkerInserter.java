package rest.create;

import com.google.gson.reflect.TypeToken;
import core.JdbiInstanceManager;
import core.TokenManager;
import core.exceptions.AbsentTokenException;
import core.exceptions.DBQueryExecutionException;
import json.CURequest;
import json.GeoCoordinates;
import json.OSMAddressNode;
import org.jdbi.v3.core.Handle;
import org.springframework.ui.Model;
import rest.read.GeoCoding;
import utils.Logging;
import utils.SQL;
import utils.Utils;

import java.lang.reflect.Type;
import java.util.Optional;

public class MarkerInserter {

    public static String addMarker(String body, Model model) throws Exception {

        Logging.println(body);
        final Type type = new TypeToken<CURequest<GeoCoordinates>>(){}.getType();
        final CURequest<GeoCoordinates> stub = Utils.gson.fromJson(body, type);

        final GeoCoordinates coordinates = stub.getContent();
        final String registration = stub.getToken();

        if (TokenManager.getInstance().hasToken(registration)) {
            Optional<OSMAddressNode> reversedCoordinates = GeoCoding.reverse(coordinates);

            Handle handler = JdbiInstanceManager.getInstance().getConnector().open();

            final Integer responseValue = reversedCoordinates
                    .map(node -> handler
                            .createUpdate(SQL.insertMarkerQuery)
                            .bind("x", coordinates.getLng()).bind("y", coordinates.getLat())
                            .bind("country", Utils.stringify(node.getCountry()))
                            .bind("country_code", Utils.stringify(node.getCountryCode()))
                            .bind("region", Utils.stringify(node.getRegion()))
                            .bind("county", Utils.stringify(node.getCounty()))
                            .bind("city", Utils.stringify(node.getCity()))
                            .bind("district", Utils.stringify(node.getDistrict()))
                            .bind("suburb", Utils.stringify(node.getSuburb()))
                            .bind("town", Utils.stringify(node.getTown()))
                            .bind("village", Utils.stringify(node.getVillage()))
                            .bind("place", Utils.stringify(node.getPlace()))
                            .bind("postcode", Utils.stringify(node.getPostcode()))
                            .bind("neighbourhood", Utils.stringify(node.getNeighbourhood()))
                            .bind("road", Utils.stringify(node.getRoad()))
                            .bind("house_number", node.getHouseNumber())
                            .execute()
                    ).orElse(-1);

            handler.close();

            if(responseValue == -1) {
                throw new DBQueryExecutionException("Error occured during the marker adding");
            }

            return "Marker successfully added";
        } else {
            throw new AbsentTokenException(registration);
        }

    }
}
