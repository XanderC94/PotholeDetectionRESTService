package rest.read;

import core.JdbiSingleton;
import core.exceptions.MarkerNotFoundException;
import json.GeoCoordinates;
import json.Marker;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.Query;
import org.springframework.ui.Model;
import utils.Logging;
import utils.SQL;
import utils.Utils;

import java.util.List;

public class AreaSelector {

    public static Marker getMarkerAt(String point, Model model) throws Exception {

        GeoCoordinates coordinates = GeoCoordinates.fromString(point);

        List<Marker> res = getElementsInArea(coordinates, Utils.minimumMetersVariation);

        Logging.println(res);

        if(res.isEmpty()){
            throw new MarkerNotFoundException("No marker found in a range of 1 meter from the specified coordinates: " +
                    coordinates.toString());
        }

        return res.get(0);
    }


    public static List<Marker> getMarkersInArea(String origin, Double radius, Model model) throws Exception {

        GeoCoordinates gcOrigin = GeoCoordinates.fromString(origin);

        return getElementsInArea(gcOrigin, radius);
    }

    private static List<Marker> getElementsInArea(GeoCoordinates origin, Double searchRadius) throws Exception {

        Handle handler = JdbiSingleton.getInstance().open();

        Query q = handler.select(SQL.selectInAreaQuery);

        // Need to bind
        q.bind("y", origin.getLat())
                .bind("x", origin.getLng())
                .bind("radius", searchRadius);

        List<Marker> res = Utils.resolveQuery(q);

        handler.close();

        return res;
    }
}
