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
    @RequestMapping(method = RequestMethod.GET, value = "/route", headers="Content-Type=application/json; charset=utf-8")
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
            Matcher matcher2 = matrixRegex.matcher(matcher1.group(1).trim());

            List<GeoCoordinates> vertices = new ArrayList<>();

            while (matcher2.find()) {

                Optional<GeoCoordinates> gc =
                        Arrays.stream(matcher2.group(1).trim().split(","))
                            .map(Double::valueOf).map(d -> new GeoCoordinates(d, 0.0))
                            .reduce((lng, lat) -> new GeoCoordinates(lat.getLat(), lng.getLat()));

                gc.ifPresent(vertices::add);
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

            return new RESTResource<>(counter.incrementAndGet(), new ArrayList<>(results));

        } else {

            return new RESTResource<List<Marker>>(counter.incrementAndGet(), new ArrayList<>())
                    .withInfo("{" +
                                "\"info\":\"There is no viable route from "+ from + " to " + to + "\", " +
                                "\"queryOutput\":" + bodyCache +
                            "}");
        }
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/area", headers="Content-Type=application/json; charset=utf-8")
    public @ResponseBody RESTResource<List<Marker>> area(@RequestParam("tlc") String tlc,
                                                          @RequestParam("brc") String brc, Model model) throws Exception {

        GeoCoordinates
                gcTLC = GeoCoordinates.fromString(tlc),
                gcBRC = GeoCoordinates.fromString(brc);

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
                        "ST_AsGeoJSON(coordinates)::json->'coordinates' AS coordinates" +
                    "FROM markers " +
                    "WHERE markers.coordinates && " +
                        "ST_Transform(" +
                            "ST_MakeEnvelope(:min_lat, :min_lng, :max_lat, :max_lng, 4326)," +
                            "4326" + //SRID
                        ");"
        );

        // Need to bind
        q.bind("lat_A", gcTLC.getLat())
        .bind("lng_A", gcTLC.getLng())
        .bind("lat_B", gcBRC.getLat())
        .bind("lng_B", gcBRC.getLng());

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
    @RequestMapping(method = RequestMethod.GET, value = "/", headers="Content-Type=application/json; charset=utf-8")
    public @ResponseBody RESTResource<List<Marker>> collect(Model model) {

        return getResources(defaultCountry, defaultRegion, defaultCounty, defaultTown, defaultRoad, model);

    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/{country}", headers="Content-Type=application/json; charset=utf-8")
    public @ResponseBody RESTResource<List<Marker>> collect(
            @PathVariable(value = "country") String country,
            Model model) {

        return getResources(country, defaultRegion, defaultCounty, defaultTown, defaultRoad, model);

    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/{country}/{region}", headers="Content-Type=application/json; charset=utf-8")
    public @ResponseBody RESTResource<List<Marker>> collect(
            @PathVariable(value = "country") String country,
            @PathVariable(value = "region") String region,
            Model model) {

        return getResources(country, region, defaultCounty, defaultTown, defaultRoad, model);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/{country}/{region}/{county}", headers="Content-Type=application/json; charset=utf-8")
    public @ResponseBody RESTResource<List<Marker>> collect(
            @PathVariable(value = "country") String country,
            @PathVariable(value = "region") String region,
            @PathVariable(value = "county") String county,
            Model model) {

        return getResources(country, region, county, defaultTown, defaultRoad, model);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/{country}/{region}/{county}/{town}", headers="Content-Type=application/json; charset=utf-8")
    public @ResponseBody RESTResource<List<Marker>> collect(
            @PathVariable(value = "country") String country,
            @PathVariable(value = "region") String region,
            @PathVariable(value = "county") String county,
            @PathVariable(value = "town") String town,
            Model model) {

        return getResources(country, region, county, town, defaultRoad, model);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/road/{road}", headers="Content-Type=application/json; charset=utf-8")
    public @ResponseBody RESTResource<List<Marker>> road(
            @PathVariable(value = "road") String road,
            Model model) {

        return getResources(defaultCountry, defaultRegion, defaultCounty, defaultTown, road, model);

    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/{country}/{region}/{county}/{town}/{road}", headers="Content-Type=application/json; charset=utf-8")
    public @ResponseBody RESTResource<List<Marker>> collect(
            @PathVariable(value = "country") String country,
            @PathVariable(value = "region") String region,
            @PathVariable(value = "county") String county,
            @PathVariable(value = "town") String town,
            @PathVariable(value = "road") String road,
            Model model) {

        return getResources(country, region, county, town, road, model);

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
    @RequestMapping(method = RequestMethod.POST, value = "/", headers="Content-Type=application/json; charset=utf-8")
    public @ResponseBody RESTResource<Integer> add(@RequestBody String body, Model model) throws Exception {

        GeoCoordinates coordinates = gson.fromJson(body, GeoCoordinates.class);

        Optional<OSMAddressNode> reversedCoordinates = reverseGeoCode(coordinates);

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
    @RequestMapping(method = RequestMethod.PUT, value = "/{id}/", headers="Content-Type=application/json; charset=utf-8")
    public @ResponseBody RESTResource<Integer> comment(@PathVariable Integer MID,
                                                       @RequestBody String body,
                                                       Model model) throws Exception {

        final Comment comment = gson.fromJson(body, Comment.class);

        Handle handler = JdbiSingleton.getInstance().open();

        Integer res = handler.createUpdate("INSERT INTO Comments(mid, comment) VALUES (:mid, :comment);")
                .bind("mid", comment.getMarkerID())
                .bind("comment", stringify(comment.getComment()))
                .execute();

        handler.close();

        return new RESTResource<>(counter.incrementAndGet(), res);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/reverse", headers="Content-Type=application/json; charset=utf-8")
    public @ResponseBody RESTResource<OSMAddressNode> reverse(@RequestParam("coordinates") String point, Model model) throws Exception {

        GeoCoordinates coordinates = GeoCoordinates.fromString(point);

        return new RESTResource<>(
                counter.incrementAndGet(),
                reverseGeoCode(coordinates).orElse(OSMAddressNode.emptyNode())
        );
    }

    private Optional<OSMAddressNode> reverseGeoCode(final GeoCoordinates coordinates) throws Exception {

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
}
