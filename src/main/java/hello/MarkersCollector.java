package hello;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import json.Marker;
import json.Point;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.Query;
import org.springframework.web.bind.annotation.*;
import rest.RESTResource;

@RestController
public class MarkersCollector {

    private final String areaDefault = "Any";
    private final AtomicLong counter = new AtomicLong();

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/collect")
    public RESTResource<List<Point>> collect(
            @RequestParam(value = "area", defaultValue = areaDefault) String area) {

        Handle handler = JdbiSingleton.getInstance().open();

        Query q = handler.select(
                "SELECT " +
                        "Location_Detection AS LocDet, " +
                        "ST_AsGeoJSON(Coordinates)::json->'coordinates' AS Coordinates " +
                    "FROM markers" + (area.equals(areaDefault) ? "" : " WHERE Location_Detection ILIKE :area")
        );

        if (!area.equals(areaDefault)) {
            q = q.bind("area", area);
        }

        List<Marker> res = q.map((rs, ctx) -> new Marker(
                new Point(rs.getString("Coordinates")),
                rs.getString("LocDet"))
        ).list();

        handler.close();

        return new RESTResource<>(counter.incrementAndGet(),
                res.stream().map(m-> m.coordinates).collect(Collectors.toList()));
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.POST, value = "/add")
    public RESTResource<Integer> add(@RequestParam(value = "lat", required = true) Double lat,
                                     @RequestParam(value = "lng", required = true) Double lng,
                                     @RequestParam(value = "area", required = true) String area) {

        Handle handler = JdbiSingleton.getInstance().open();

        Integer ret = handler.createUpdate(
                "INSERT " +
                        "INTO Markers(Coordinates, Location_Detection) " +
                        "VALUES (" +
                            "ST_SetSRID(ST_MakePoint(:lat, :lng), 4326), " +
                            ":area" +
                        ");"
        ).bind("lat", lat).bind("lng", lng).bind("area", area).execute();

        handler.close();

        return new RESTResource<>(counter.incrementAndGet(), ret);
    }
}
