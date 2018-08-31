package core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import core.exceptions.DBQueryExecutionException;
import core.exceptions.FormatException;
import core.exceptions.MarkerNotFoundException;
import core.exceptions.WrongBodyDataException;
import json.*;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.Query;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import rest.RESTResource;
import utils.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static utils.Utils.*;

/**
 *
 *
 */
@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/pothole/")
public class RestAPIController {

    private static final String UTF_8 = java.nio.charset.StandardCharsets.UTF_8.name();

    private static final String ORS_API_KEY = "5b3ce3597851110001cf6248c396a2051da84b2ea36fa8e7f8f99d89";

    private final String geoCodingURLFormat =
            "https://nominatim.openstreetmap.org/search?q=%s&format=%s&polygon_geojson=1&limit=%d";
    private final String reverseGeoCodingURLFormat =
            "https://nominatim.openstreetmap.org/reverse?lon=%.9f&lat=%.9f&format=%s";

    private final String openRoutingServiceURLFormat =
            "https://api.openrouteservice.org/directions?" +
                "api_key=%s&coordinates=%.9f,%.9f|%.9f,%.9f&profile=%s&preference=%s&geometry_format=%s";

    private static final Gson gson = new GsonBuilder().create();

    private final String defaultTown = "none";
    private final String defaultCounty = "none";
    private final String defaultCountry = "none";
    private final String defaultRegion = "none";
    private final String defaultRoad = "none";

    // a variation of 0.0000089 degrees corresponds approximately to 1m variation
    private final Double minimumDegreesVariation = 0.0000089;
    private final Double minimumMetersVariation = 1.0;

    private final AtomicLong counter = new AtomicLong();

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "")
    public @ResponseBody RESTResource<List<Marker>> collect(Model model) throws Exception {

        return getResources(defaultCountry, defaultRegion, defaultCounty, defaultTown, defaultRoad, model);

    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/{country}")
    public @ResponseBody RESTResource<List<Marker>> collect(
            @PathVariable(value = "country") String country,
            Model model) throws Exception {

        return getResources(country, defaultRegion, defaultCounty, defaultTown, defaultRoad, model);

    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/{country}/{region}")
    public @ResponseBody RESTResource<List<Marker>> collect(
            @PathVariable(value = "country") String country,
            @PathVariable(value = "region") String region,
            Model model) throws Exception {

        return getResources(country, region, defaultCounty, defaultTown, defaultRoad, model);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/{country}/{region}/{county}")
    public @ResponseBody RESTResource<List<Marker>> collect(
            @PathVariable(value = "country") String country,
            @PathVariable(value = "region") String region,
            @PathVariable(value = "county") String county,
            Model model) throws Exception {

        return getResources(country, region, county, defaultTown, defaultRoad, model);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/{country}/{region}/{county}/{town}")
    public @ResponseBody RESTResource<List<Marker>> collect(
            @PathVariable(value = "country") String country,
            @PathVariable(value = "region") String region,
            @PathVariable(value = "county") String county,
            @PathVariable(value = "town") String town,
            Model model) throws Exception {

        return getResources(country, region, county, town, defaultRoad, model);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/{country}/{region}/{county}/{town}/{road}")
    public @ResponseBody RESTResource<List<Marker>> collect(
            @PathVariable(value = "country") String country,
            @PathVariable(value = "region") String region,
            @PathVariable(value = "county") String county,
            @PathVariable(value = "town") String town,
            @PathVariable(value = "road") String road,
            Model model) throws Exception {

        return getResources(country, region, county, town, road, model);

    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/road/{road_name}")
    public @ResponseBody RESTResource<List<Marker>> road(
            @PathVariable(value = "road_name") String road,
            Model model) throws Exception {

        return getResources(defaultCountry, defaultRegion, defaultCounty, defaultTown, road, model);
    }

    private RESTResource<List<Marker>> getResources(
            String country, String region, String county, String town, String road, Model model
    ) throws Exception {

        Handle handler = JdbiSingleton.getInstance().open();

        Query q = handler.select(SQL.selectMarkersQuery
                .apply(filters(country, region, county, town, road)));

        if (!country.toLowerCase().equals(defaultCountry)) {
            q = q.bind("country", Utils.stringify(country));
        }

        if (!region.toLowerCase().equals(defaultRegion)) {
            q = q.bind("region", Utils.stringify(region));
        }

        if (!county.toLowerCase().equals(defaultCounty)) {
            q = q.bind("county", Utils.stringify(county));
        }

        if (!town.toLowerCase().equals(defaultTown)) {
            q = q.bind("town", Utils.stringify(town));
            q = q.bind("city", Utils.stringify(town));
        }

        if (!road.toLowerCase().equals(defaultRoad)) {
            q = q.bind("road", Utils.stringify(road));
        }

        List<Marker> res = resolveQuery(q);

        handler.close();

        println(res.stream().map(r -> r.getCoordinates().toString()).collect(Collectors.toList()));

        return new RESTResource<>(counter.incrementAndGet(), res);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/route")
    public @ResponseBody RESTResource<List<Marker>> route(@RequestParam("from") String from,
                                                          @RequestParam("to") String to,
                                                          @RequestParam(value = "dist", required = false, defaultValue = "100") Integer dist,
                                                          @RequestParam(value = "mode", required = false, defaultValue = "driving-car") String mode,
                                                          @RequestParam(value = "route", required = false, defaultValue = "recommended") String route,
                                                          Model model) throws Exception {

        Tuple<Optional<GeoCoordinates>, String>
                testFrom = Utils.checkCoordinatesFormat(from),
                testTo = Utils.checkCoordinatesFormat(to);

        log((testFrom.getX().isPresent() || testTo.getX().isPresent() ?
                "Routing by Coordinates..." : "Routing by place...")
                + "from " + from + " to " + to
        );

        Optional<GeoCoordinates>
            optFrom = Optional.ofNullable(testTo.getX().orElse(this.geoCoding(from).orElse(null))),
            optTo = Optional.ofNullable(testTo.getX().orElse(this.geoCoding(to).orElse(null)));

        GeoCoordinates
                orig = optFrom.orElseThrow(() -> new FormatException(testFrom.getY() + " or Invalid Location")),
                dest = optTo.orElseThrow(() -> new FormatException(testTo.getY() + " or Invalid Location"));

        log(Arrays.asList(orig, dest));

        final String url = String.format(openRoutingServiceURLFormat, ORS_API_KEY,
                orig.getLng(), orig.getLat(),
                dest.getLng(), dest.getLat(),
                mode, route, "geojson"
                );

        log(url);

        final String bodyCache = HTTP.get(url);

        final Matcher matcher1 = routingRegex.matcher(bodyCache);

        if (matcher1.find()) {

            println(matcher1.group(1).trim());

            final Matcher matcher2 = matrixRegex.matcher(matcher1.group(1).trim());

            final List<GeoCoordinates> vertices = new ArrayList<>();

            while (matcher2.find()) {
                vertices.add(GeoCoordinates.fromString(matcher2.group(0)));
            }

            final Handle handler = JdbiSingleton.getInstance().open();

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

                results.addAll(this.resolveQuery(q));
            }

            handler.close();

            return new RESTResource<List<Marker>>(counter.incrementAndGet(), new ArrayList<>(results))
                    .withInfo(bodyCache);

        } else {

            return new RESTResource<List<Marker>>(counter.incrementAndGet(), new ArrayList<>())
                    .withInfo("{" +
                            "\"info\":\"There is no viable route from "+ from + " to " + to + "\", " +
                            "\"queryOutput\":" + bodyCache +
                            "}");
        }
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/at")
    public @ResponseBody RESTResource<Marker> getMarkerAt(@RequestParam("coordinates") String point,
                                                          Model model) throws Exception {

        GeoCoordinates coordinates = GeoCoordinates.fromString(point);

        List<Marker> res = this.getElementsInArea(coordinates, this.minimumMetersVariation);

        println(res);

        if(res.isEmpty()){
            throw new MarkerNotFoundException("No marker found in a range of 1 meter from the specified coordinates: " +
                    coordinates.toString());
        }

        return  new RESTResource<>(counter.incrementAndGet(), res.get(0));
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/area")
    public @ResponseBody RESTResource<List<Marker>> area(@RequestParam("origin") String origin,
                                                         @RequestParam("radius") Double radius,
                                                         Model model) throws Exception {

        GeoCoordinates gcOrigin = GeoCoordinates.fromString(origin);

        return new RESTResource<>(counter.incrementAndGet(), getElementsInArea(gcOrigin, radius));
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/geodecode")
    public @ResponseBody RESTResource<GeoCoordinates> geodecode(
            @RequestParam("place") String place, Model model) {

        return new RESTResource<>(
                counter.incrementAndGet(),
                geoCoding(place).orElse(GeoCoordinates.empty())
        );
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/reverse")
    public @ResponseBody RESTResource<OSMAddressNode> reverse(
            @RequestParam("coordinates") String coordinates, Model model) throws Exception {

        return new RESTResource<>(
                counter.incrementAndGet(),
                reverseGeoCoding(GeoCoordinates.fromString(coordinates))
                        .orElse(OSMAddressNode.empty())
        );
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.POST, value = "", headers="Content-Type=application/json; charset=utf-8")
    public @ResponseBody RESTResource<Integer> add(@RequestBody String body, Model model) {

        Utils.println(body);

        GeoCoordinates coordinates = gson.fromJson(body, GeoCoordinates.class);

        Optional<OSMAddressNode> reversedCoordinates = reverseGeoCoding(coordinates);

        Handle handler = JdbiSingleton.getInstance().open();

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

        return new RESTResource<>(counter.incrementAndGet(), responseValue);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.PUT, value = "/{id}", headers="Content-Type=application/json; charset=utf-8")
    public @ResponseBody RESTResource<Integer> comment(@PathVariable Integer id,
                                                       @RequestBody String body,
                                                       Model model) throws Exception{

        final Comment comment = gson.fromJson(body, Comment.class);

        if (comment.getMarkerID() != id) {
            throw new WrongBodyDataException("Mismatch between PathVariable markerId ("+ id + ") and body markerID (" + comment.getMarkerID() + ")");
        }

        Handle handler = JdbiSingleton.getInstance().open();


        Integer res;
        try {
            res = handler
                    .createUpdate(SQL.insertCommentToMarkerQuery)
                    .bind("marker_id", comment.getMarkerID())
                    .bind("text", stringify(comment.getText()))
                    .execute();
        } catch (Exception e) {
            throw new DBQueryExecutionException("Unable to execute statement on the DB");
        }

        handler.close();

        return new RESTResource<>(counter.incrementAndGet(), res);
    }

    private List<Marker> resolveQuery(final Query q) throws Exception{
        List<Nuple<Integer, String, String>> resultSets =
                q.map((rs, ctx) -> Utils.nuple(
                        rs.getInt("ID"),
                        rs.getString("coordinates"),
                        rs.getString("addressNode")
                )).list();

        List<Marker> res = new ArrayList<>();

//        println(resultSets);

        for (Nuple<Integer, String, String> n : resultSets) {
            res.add(new Marker(
                    Long.valueOf(n.getX()), 0,
                    GeoCoordinates.fromString(n.getY()),
                    gson.fromJson(n.getZ(), OSMAddressNode.class)
            ));
        }

        return res;
    }

    private Optional<GeoCoordinates> geoCoding(final String place) {

        final String bodyCache;

        try {

            final String url = String.format(geoCodingURLFormat, URLEncoder.encode(place, UTF_8), "jsonv2", 1);

            log(url);

            bodyCache = HTTP.get(url);

        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }

        Matcher matcher = coordinatesRegex.matcher(bodyCache);

        if (matcher.find()) {
            String coordinates = matcher.group(0).replaceFirst("lon", "lng");

            return Optional.of(gson.fromJson("{" + coordinates + "}", GeoCoordinates.class));
        } else {
            return Optional.empty();
        }
    }

    private Optional<OSMAddressNode> reverseGeoCoding(final GeoCoordinates coordinates) {

        final String bodyCache;
        try {

            final String url = String.format(
                    reverseGeoCodingURLFormat,
                    coordinates.getLng(), coordinates.getLat(),
                    "jsonv2"
            );

            log(url);

            bodyCache = HTTP.get(url);

        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }

        Matcher matcher = addressRegex.matcher(bodyCache);

        if (matcher.find()) {
            String address = matcher.group(1);

            address = address.replaceFirst("address[0-9]+", "place")
                    .replaceFirst("country_code", "countryCode")
                    .replaceFirst("house_number", "houseNumber")
                    .replaceFirst("state", "region")
                    .replaceFirst("city_district", "district");

            return Optional.of(gson.fromJson("{" + address + "}", OSMAddressNode.class));
        } else {
            return Optional.empty();
        }
    }

    private List<Marker> getElementsInArea(GeoCoordinates origin, Double searchRadius) throws Exception {

        Handle handler = JdbiSingleton.getInstance().open();

        Query q = handler.select(SQL.selectInAreaQuery);

        // Need to bind
        q.bind("y", origin.getLat())
         .bind("x", origin.getLng())
         .bind("radius", searchRadius);

        List<Marker> res = this.resolveQuery(q);

        handler.close();

        return res;
    }

    private String filters(final String country, final String region, final String county, final String town, final String road) {

        final Map<String, Boolean> enabledFilters = new HashMap<>();
        final Map<String, Boolean> specialFilters = new HashMap<>();

        enabledFilters.put("country", !country.toLowerCase().equals(defaultCountry));
        enabledFilters.put("region", !region.toLowerCase().equals(defaultRegion));
        enabledFilters.put("county", !county.toLowerCase().equals(defaultCounty));
        enabledFilters.put("road", !road.toLowerCase().equals(defaultRoad));

        specialFilters.put("town", !town.toLowerCase().equals(defaultTown));
        specialFilters.put("city", !town.toLowerCase().equals(defaultTown));

        final List<String> eFilters = enabledFilters.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(e-> e.getKey() + " ILIKE :" + e.getKey().toLowerCase()+"")
                .collect(Collectors.toList());

        final List<String> sFilters = specialFilters.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(e-> e.getKey() + " ILIKE :" + e.getKey().toLowerCase()+"")
                .collect(Collectors.toList());

        final String eFilter = " WHERE " + String.join(" AND ", eFilters);
        final String sFilter = String.join(" OR ", sFilters) ;

        final String filter = String.join(" AND ", eFilter, "(" + sFilter + ")");

        println(filter);

        return eFilters.isEmpty() && sFilters.isEmpty() ? "" :
                eFilters.isEmpty() ? sFilter :
                sFilter.isEmpty() ? eFilter :
                filter;
    }
}
