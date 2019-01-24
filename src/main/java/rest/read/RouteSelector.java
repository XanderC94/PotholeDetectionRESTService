package rest.read;

import core.JdbiInstanceManager;
import core.exceptions.FormatException;
import json.*;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.Query;
import org.springframework.ui.Model;
import utils.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static utils.Regex.coordinatesTuple;
import static utils.Regex.routeGeometry;

public class RouteSelector {

    public static RoutesResponse route(
            String from,String to, Integer dist, String mode, String route, Model model
    ) throws Exception {

        Tuple<Optional<GeoCoordinates>, String>
                testFrom = Formatting.checkCoordinatesFormat(from),
                testTo = Formatting.checkCoordinatesFormat(to);

        Logging.log((testFrom.getX().isPresent() || testTo.getX().isPresent() ?
                "Routing by Coordinates..." : "Routing by place...")
                + "from " + from + " to " + to
        );

        Optional<GeoCoordinates>
                optFrom = Optional.ofNullable(testTo.getX().orElse(GeoCoding.decode(from).orElse(null))),
                optTo = Optional.ofNullable(testTo.getX().orElse(GeoCoding.decode(to).orElse(null)));

        GeoCoordinates
                orig = optFrom.orElseThrow(() -> new FormatException(testFrom.getY() + " or Invalid Location")),
                dest = optTo.orElseThrow(() -> new FormatException(testTo.getY() + " or Invalid Location"));

        Logging.log(Arrays.asList(orig, dest));

        final String url = String.format(Utils.openRoutingServiceURLFormat, Utils.ORS_API_KEY,
                orig.getLng(), orig.getLat(),
                dest.getLng(), dest.getLat(),
                mode, route, "geojson"
        );

        Logging.log(url);

        final String bodyCache = HTTP.get(url);

        final Matcher matcher1 = routeGeometry.matcher(bodyCache);

        if (matcher1.find()) {

//            println(matcher1.group(1).trim());

            final Matcher matcher2 = coordinatesTuple.matcher(matcher1.group(1).trim());

            final List<GeoCoordinates> vertices = new ArrayList<>();

            while (matcher2.find()) {
                vertices.add(GeoCoordinates.fromString(matcher2.group(0)));
            }

            final Handle handler = JdbiInstanceManager.getInstance().getConnector().open();

            final Query q = handler.select(SQL.selectOnRouteQuery);

            final Set<Marker> results = new HashSet<>();

            final List<Segment> segments = IntStream.range(1, vertices.size())
                    .mapToObj(i -> new Segment(vertices.get(i-1), vertices.get(i)))
                    .collect(Collectors.toList());

            for (Segment s : segments) {
                // NOTE: Latitude (X) and Longitude (Y) are the angles of in degrees
                // of a point on the sphere surface from the Origin.
                // Need to bind
                q.bind("y_A", s.getA().getLat())
                        .bind("x_A", s.getA().getLng())
                        .bind("y_B", s.getB().getLat())
                        .bind("x_B", s.getB().getLng())
                        .bind("dist", dist);

                results.addAll(Utils.resolveQuery(q));
            }

            handler.close();

//            return new RESTResponse<List<Marker>>(counter.incrementAndGet(), new ArrayList<>(results))
//                    .withInfo(bodyCache);

            RoutesResponse response = new RoutesResponse(new ArrayList<>(results), bodyCache);

            return response;

        } else {

            return new RoutesResponse(new ArrayList<>(), bodyCache);
        }
    }
}
