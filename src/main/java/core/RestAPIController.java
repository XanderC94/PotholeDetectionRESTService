package core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import json.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.Query;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import rest.RESTResource;
import utils.Utils;

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
@RestController
@RequestMapping("/api/pothole/")
public class RestAPIController {

    private static final String ORS_API_KEY = "5b3ce3597851110001cf6248c396a2051da84b2ea36fa8e7f8f99d89";

    private static final Gson gson = new GsonBuilder().create();

    private final String defaultTown = "none";
    private final String defaultCounty = "none";
    private final String defaultCountry = "none";
    private final String defaultRegion = "none";
    private final String defaultRoad = "none";

    private final AtomicLong counter = new AtomicLong();

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "")
    public @ResponseBody RESTResource<List<Marker>> collect(Model model) {

        return getResources(defaultCountry, defaultRegion, defaultCounty, defaultTown, defaultRoad, model);

    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/{country}")
    public @ResponseBody RESTResource<List<Marker>> collect(
            @PathVariable(value = "country") String country,
            Model model) {

        return getResources(country, defaultRegion, defaultCounty, defaultTown, defaultRoad, model);

    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/{country}/{region}")
    public @ResponseBody RESTResource<List<Marker>> collect(
            @PathVariable(value = "country") String country,
            @PathVariable(value = "region") String region,
            Model model) {

        return getResources(country, region, defaultCounty, defaultTown, defaultRoad, model);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/{country}/{region}/{county}")
    public @ResponseBody RESTResource<List<Marker>> collect(
            @PathVariable(value = "country") String country,
            @PathVariable(value = "region") String region,
            @PathVariable(value = "county") String county,
            Model model) {

        return getResources(country, region, county, defaultTown, defaultRoad, model);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/{country}/{region}/{county}/{town}")
    public @ResponseBody RESTResource<List<Marker>> collect(
            @PathVariable(value = "country") String country,
            @PathVariable(value = "region") String region,
            @PathVariable(value = "county") String county,
            @PathVariable(value = "town") String town,
            Model model) {

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
            Model model) {

        return getResources(country, region, county, town, road, model);

    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/road/{road_name}")
    public @ResponseBody RESTResource<List<Marker>> road(
            @PathVariable(value = "road_name") String road,
            Model model) {

        return getResources(defaultCountry, defaultRegion, defaultCounty, defaultTown, road, model);

    }


    private RESTResource<List<Marker>> getResources(String country, String region, String county, String town, String road, Model model) {

        Handle handler = JdbiSingleton.getInstance().open();

        Query q = handler.select(
                "SELECT " +
                        "ID," +
                        "json_build_object(" +
                            "'country',country," +
                            "'countryCode',country_code," +
                            "'region',region," +
                            "'county',county," +
                            "'town',town," +
                            "'place',place," +
                            "'neighbourhood',neighbourhood," +
                            "'road',road" +
                        ") AS addressNode," +
                        "ST_AsGeoJSON(coordinates)::json->'coordinates' AS coordinates " +
                    "FROM markers " + addFilters(country, region, county, town, road) + ";"
        );

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
        }

        if (!road.toLowerCase().equals(defaultRoad)) {
            q = q.bind("road", Utils.stringify(road));
        }

        List<Marker> res = q.map((rs, ctx) -> {
                ArrayList tmp = gson.fromJson(rs.getString("coordinates"), ArrayList.class);

                return new Marker(
                        rs.getInt("ID"), 0,
                        new GeoCoordinates((Double) tmp.get(0), (Double) tmp.get(1)),
                        gson.fromJson(rs.getString("addressNode"), OSMAddressNode.class)
                );
            }
        ).list();

        handler.close();

        println(res);

        return new RESTResource<>(counter.incrementAndGet(), res);
    }

    private String addFilters(final String country, final String region, final String county, final String town, final String road) {

        final Map<String, Boolean> enabledFilters = new HashMap<>();

        enabledFilters.put("country", !country.toLowerCase().equals(defaultCountry));
        enabledFilters.put("region", !region.toLowerCase().equals(defaultRegion));
        enabledFilters.put("town", !town.toLowerCase().equals(defaultTown));
        enabledFilters.put("county", !county.toLowerCase().equals(defaultCounty));
        enabledFilters.put("road", !road.toLowerCase().equals(defaultRoad));

        final List<String> filters = enabledFilters.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(e-> e.getKey() + " ILIKE :" + e.getKey().toLowerCase()+"")
                .collect(Collectors.toList());

        final String filter = " WHERE " + String.join(" AND ", filters);

        return filters.isEmpty() ? "" : filter;
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/route")
    public @ResponseBody RESTResource<List<Marker>> route(@RequestParam("from") String from,
                                                          @RequestParam("to") String to,
                                                          @RequestParam(value = "dist", required = false, defaultValue = "100") Integer dist,
                                                          Model model) throws Exception {

        GeoCoordinates
                gcFrom = GeoCoordinates.fromString(from),
                gcTo = GeoCoordinates.fromString(to);

        OkHttpClient client = new OkHttpClient();

        Request routingService = new Request.Builder()
                .url("https://api.openrouteservice.org/directions?" +
                        "api_key=" + ORS_API_KEY +"&" +
                        "coordinates=" + gcFrom.getLng() +"," + gcFrom.getLat() +"|" +
                        gcTo.getLng() +"," + gcTo.getLat() +"&" +
                        "profile=driving-car&" +
                        "preference=recommended&" +
                        "geometry_format=geojson")
                .build();

        Response routingServiceResult = client.newCall(routingService).execute();

        assert routingServiceResult.body() != null;
        final String bodyCache = routingServiceResult.body().string();
        Matcher matcher1 = routingRegex.matcher(bodyCache);

        if (matcher1.find()) {

            println(matcher1.group(1).trim());

            Matcher matcher2 = matrixRegex.matcher(matcher1.group(1).trim());


            List<GeoCoordinates> vertices = new ArrayList<>();

            while (matcher2.find()) {
                vertices.add(GeoCoordinates.fromString(matcher2.group(0)));
            }

            Handle handler = JdbiSingleton.getInstance().open();

            Query q = handler.select(
                    "SELECT " +
                            "ID," +
                            "json_build_object(" +
                            "'country',country," +
                            "'countryCode',country_code," +
                            "'region',region," +
                            "'county',county," +
                            "'town',town," +
                            "'place',place," +
                            "'neighbourhood',neighbourhood," +
                            "'road',road" +
                            ") AS addressNode," +
                            "ST_AsGeoJSON(coordinates)::json->'coordinates' AS coordinates " +
                            "FROM markers " +
                            "WHERE ST_DistanceSphere(" +
                            "ST_SetSRID(ST_MakeLine(ST_MakePoint(:lat_A, :lng_A), ST_MakePoint(:lat_B, :lng_B)), 4326)," +
                            "markers.coordinates" +
                            ") < :dist;"
            );

            Set<Marker> results = new HashSet<>();

            IntStream.range(1, vertices.size())
                    .mapToObj(i -> new Segment(vertices.get(i-1), vertices.get(i)))
                    .forEach(v -> {

                        // NOTE: Latitude (X) and Longitude (Y) are the angles of in degrees
                        // of a point on the sphere surface from the Origin.

                        // Need to bind
                        q.bind("lat_A", v.getA().getLat())
                                .bind("lng_A", v.getA().getLng())
                                .bind("lat_B", v.getB().getLat())
                                .bind("lng_B", v.getB().getLng())
                                .bind("dist", dist);

                        results.addAll(q.map((rs, ctx) -> {
                            ArrayList tmp = gson.fromJson(rs.getString("coordinates"), ArrayList.class);

                            return new Marker(
                                    rs.getInt("ID"), 0,
                                    new GeoCoordinates((Double) tmp.get(0), (Double) tmp.get(1)),
                                    gson.fromJson(rs.getString("addressNode"), OSMAddressNode.class)
                            );
                        }).list());
                    });

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
    @RequestMapping(method = RequestMethod.GET, value = "/area")
    public @ResponseBody RESTResource<List<Marker>> area(@RequestParam("origin") String origin,
                                                         @RequestParam("radius") String radius, Model model) throws Exception {

        GeoCoordinates gcOrigin = GeoCoordinates.fromString(origin);

        Handle handler = JdbiSingleton.getInstance().open();

        Query q = handler.select(
                "SELECT " +
                        "ID," +
                        "json_build_object(" +
                        "'country',country," +
                        "'countryCode',country_code," +
                        "'region',region," +
                        "'county',county," +
                        "'town',town," +
                        "'place',place," +
                        "'neighbourhood',neighbourhood," +
                        "'road',road" +
                        ") AS addressNode," +
                        "ST_AsGeoJSON(coordinates)::json->'coordinates' AS coordinates " +
                        "FROM markers " +
                        "WHERE ST_DistanceSphere(" +
                        "ST_SetSRID(ST_MakePoint(:lat_A, :lng_A), 4326)," +
                        "markers.coordinates" +
                        ") < :radius;"
        );

        // Need to bind
        q.bind("lat_A", gcOrigin.getLat())
                .bind("lng_A", gcOrigin.getLng())
                .bind("radius", radius);

        handler.close();

        List<Marker> res = q.map((rs, ctx) -> {
                    ArrayList tmp = gson.fromJson(rs.getString("coordinates"), ArrayList.class);

                    return new Marker(
                            rs.getInt("ID"), 0,
                            new GeoCoordinates((Double) tmp.get(0), (Double) tmp.get(1)),
                            gson.fromJson(rs.getString("addressNode"), OSMAddressNode.class)
                    );
                }
        ).list();

        return new RESTResource<>(counter.incrementAndGet(), res);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/geodecode")
    public @ResponseBody RESTResource<GeoCoordinates> geodecode(@RequestParam("place") String place, Model model) throws Exception {

        return new RESTResource<>(
                counter.incrementAndGet(),
                geoCoding(place).orElse(GeoCoordinates.empty())
        );
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/reverse")
    public @ResponseBody RESTResource<OSMAddressNode> reverse(@RequestParam("coordinates") String coordinates, Model model) throws Exception {

        GeoCoordinates gc = GeoCoordinates.fromString(coordinates);

        return new RESTResource<>(
                counter.incrementAndGet(),
                reverseGeoCoding(gc).orElse(OSMAddressNode.empty())
        );
    }

    private Optional<GeoCoordinates> geoCoding(final String place) throws Exception {

        OkHttpClient client = new OkHttpClient();

        Request reverseGeoCoding = new Request.Builder()
                .url("https://nominatim.openstreetmap.org/search/"+ place + "?format=jsonv2&limit=1")
                .build();

        Response reverseGeoCodingResult = client.newCall(reverseGeoCoding).execute();

        assert reverseGeoCodingResult.body() != null;
        final String bodyCache = reverseGeoCodingResult.body().string();
        Matcher matcher = coordinatesRegex.matcher(bodyCache);

        if (matcher.find()) {
            String coordinates = matcher.group(0).replaceFirst("lon", "lng");

            return Optional.of(gson.fromJson("{" + coordinates + "}", GeoCoordinates.class));
        } else {
            return Optional.empty();
        }
    }

    private Optional<OSMAddressNode> reverseGeoCoding(final GeoCoordinates coordinates) throws Exception {

        OkHttpClient client = new OkHttpClient();

        Request reverseGeoCoding = new Request.Builder()
                .url("https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=" + coordinates.getLat() + "&lon=" + coordinates.getLng())
                .build();

        Response reverseGeoCodingResult = client.newCall(reverseGeoCoding).execute();

        assert reverseGeoCodingResult.body() != null;
        Matcher matcher = addressRegex.matcher(reverseGeoCodingResult.body().string());

        if (matcher.find()) {
            String address = matcher.group(1);

            address = address.replaceFirst("address[0-9]+", "place")
                    .replaceFirst("suburb", "town")
                    .replaceFirst("village", "neighbourhood")
                    .replaceFirst("country_code", "countryCode")
                    .replaceFirst("house_number", "houseNumber")
                    .replaceFirst("state", "region");

            return Optional.of(gson.fromJson("{" + address + "}", OSMAddressNode.class));
        } else {
            return Optional.empty();
        }
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.POST, value = "", headers="Content-Type=application/json; charset=utf-8")
    public @ResponseBody RESTResource<Integer> add(@RequestBody String body, Model model) throws Exception {

        GeoCoordinates coordinates = gson.fromJson(body, GeoCoordinates.class);

        Optional<OSMAddressNode> reversedCoordinates = reverseGeoCoding(coordinates);

        Handle handler = JdbiSingleton.getInstance().open();

        final Integer responseValue = reversedCoordinates
                .map(node -> handler.createUpdate(
                    "INSERT " +
                            "INTO Markers(" +
                                "coordinates, country, country_code, region, county, " +
                                "town, place, postcode, neighbourhood, road, house_number" +
                            ") " +
                            "VALUES (" +
                                "ST_SetSRID(ST_MakePoint(:lat, :lng), 4326)," +
                                ":country," +
                                ":country_code," +
                                ":region," +
                                ":county," +
                                ":town," +
                                ":place," +
                                ":postcode," +
                                ":neighbourhood," +
                                ":road," +
                                ":house_number" +
                            ");"
                        ).bind("lat", coordinates.getLat()).bind("lng", coordinates.getLng())
                            .bind("country", Utils.stringify(node.getCountry()))
                            .bind("country_code", Utils.stringify(node.getCountryCode()))
                            .bind("region", Utils.stringify(node.getRegion()))
                            .bind("county", Utils.stringify(node.getCounty()))
                            .bind("town", Utils.stringify(node.getTown()))
                            .bind("place", Utils.stringify(node.getPlace()))
                            .bind("postcode", Utils.stringify(node.getPostcode()))
                            .bind("neighbourhood", Utils.stringify(node.getNeighbourhood()))
                            .bind("road", Utils.stringify(node.getRoad()))
                            .bind("house_number", node.getHouseNumber())
                                .execute()
                ).orElse(-1);

        handler.close();

        return new RESTResource<>(counter.incrementAndGet(), responseValue);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.PUT, value = "/{id}", headers="Content-Type=application/json; charset=utf-8")
    public @ResponseBody RESTResource<Integer> comment(@PathVariable Integer id,
                                                       @RequestBody String body,
                                                       Model model) throws Exception{

        final Comment comment = gson.fromJson(body, Comment.class);

        if (comment.getMarkerID() != id) throw new Exception("Mismatch between PathVariable MID and body MID");

        Handle handler = JdbiSingleton.getInstance().open();

        String info = "";
        Integer res = -1;

        try {
            res = handler.createUpdate("INSERT INTO Comments(mid, comment) VALUES (:mid, :comment);")
                    .bind("mid", comment.getMarkerID())
                    .bind("comment", stringify(comment.getComment()))
                    .execute();

        } catch(Exception ex) {
            info = ex.getClass().getName();
        }

        handler.close();

        return new RESTResource<>(counter.incrementAndGet(), res)
                .withInfo(info);
    }
}
