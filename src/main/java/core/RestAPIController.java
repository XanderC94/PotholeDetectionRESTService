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

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static utils.Utils.println;

/**
 *
 *
 */
@RestController
public class RestAPIController {

    private static final String ORS_API_KEY = "5b3ce3597851110001cf6248c396a2051da84b2ea36fa8e7f8f99d89";

    private static final Pattern addressRegex = Pattern.compile("(?:\"address\":)\\{(.*?)\\}");
    private static final Pattern routingRegex = Pattern.compile("(?:\"geometry\":)\\{(.*?)\\}");
    private static final Pattern arrayRegex = Pattern.compile("[+-]?\\d*\\.\\d*");
    private static final Pattern matrixRegex = Pattern.compile("\\[([+-]?\\d*\\.\\d*,[+-]?\\d*\\.\\d*)\\]");

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
                gcFrom = new GeoCoordinates(0,0),
                gcTo = new GeoCoordinates(0,0);

        Matcher
                mFrom = arrayRegex.matcher(from),
                mTo = arrayRegex.matcher(to);

        if (mFrom.find() && mTo.find()) {
            gcFrom.setLat(Double.valueOf(mFrom.group(0)));
            gcTo.setLat(Double.valueOf(mTo.group(0)));
        } else {
            throw new Exception("Coordinates must be like from=[x.y, w.z]&to=[x'.y', w'.z']");
        }

        if (mFrom.find() && mTo.find()) {
            gcFrom.setLng(Double.valueOf(mFrom.group(0)));
            gcTo.setLng(Double.valueOf(mTo.group(0)));
        } else {
            throw new Exception("Coordinates must be like from=[x.y, w.z]&to=[x'.y', w'.z']");
        }

        Set<Marker> results = new HashSet<>();

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

            Geometry geom = new Geometry("Linestring", vertices);

            Handle handler = JdbiSingleton.getInstance().open();

            Query q = handler.select(
                    "SELECT " +
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
                                    new GeoCoordinates((Double) tmp.get(0), (Double) tmp.get(1)),
                                    gson.fromJson(rs.getString("addressNode"), OSMAddressNode.class)
                            );
                        }).list());
            });

            handler.close();

        } else {

            return new RESTResource<List<Marker>>(counter.incrementAndGet(), new ArrayList<>())
                    .withInfo("{" +
                                "\"info\":\"There is no viable route from "+ from + " to " + to + "\", " +
                                "\"queryOutput\":" + bodyCache +
                            "}");
        }

        return new RESTResource<>(counter.incrementAndGet(), new ArrayList<>(results));
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/area", headers="Content-Type=application/json; charset=utf-8")
    public @ResponseBody RESTResource<List<Marker>> area(@RequestParam("tlc") String tlc,
                                                          @RequestParam("brc") String brc, Model model) throws Exception {

        GeoCoordinates
                gcTLC = new GeoCoordinates(0,0),
                gcBRC = new GeoCoordinates(0,0);

        Matcher
                mTLC = arrayRegex.matcher(tlc),
                mBRC = arrayRegex.matcher(brc);

        if (mTLC.find() && mBRC.find()) {
            gcTLC.setLat(Double.valueOf(mTLC.group(0)));
            gcBRC.setLat(Double.valueOf(mBRC.group(0)));
        } else {
            throw new Exception("Coordinates must be like from=[x.y, w.z]&to=[x'.y', w'.z']");
        }

        if (mTLC.find() && mBRC.find()) {
            gcTLC.setLng(Double.valueOf(mTLC.group(0)));
            gcBRC.setLng(Double.valueOf(mBRC.group(0)));
        } else {
            throw new Exception("Coordinates must be like from=[x.y, w.z]&to=[x'.y', w'.z']");
        }

        Handle handler = JdbiSingleton.getInstance().open();

        Query q = handler.select(
                "SELECT " +
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
                            new GeoCoordinates((Double) tmp.get(0), (Double) tmp.get(1)),
                            gson.fromJson(rs.getString("addressNode"), OSMAddressNode.class)
                    );
                }
        ).list();

        return new RESTResource<>(counter.incrementAndGet(), res);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/collect", headers="Content-Type=application/json; charset=utf-8")
    public @ResponseBody RESTResource<List<Marker>> collect(
            @RequestParam(value = "country", defaultValue = defaultCountry, required = false) String country,
            @RequestParam(value = "region", defaultValue = defaultRegion, required = false) String region,
            @RequestParam(value = "county", defaultValue = defaultTown, required = false) String county,
            @RequestParam(value = "town", defaultValue = defaultTown,required = false) String town,
            @RequestParam(value = "road", defaultValue = defaultRoad,required = false) String road,
            Model model
    ) {

        Handle handler = JdbiSingleton.getInstance().open();

        Query q = handler.select(
                "SELECT " +
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
                    "FROM markers" + addFilters(country, region, county, town, road) + ";"
        );

        if (!town.toLowerCase().equals(defaultTown)) {
            q = q.bind("town", Utils.stringify(town));
        } else if (!county.toLowerCase().equals(defaultCounty)) {
            q = q.bind("county", Utils.stringify(county));
        } else if (!country.toLowerCase().equals(defaultCountry)) {
            q = q.bind("country", Utils.stringify(country));
        } else if (!region.toLowerCase().equals(defaultRegion)) {
            q = q.bind("region", Utils.stringify(region));
        } else if (!road.toLowerCase().equals(defaultRoad)) {
            q = q.bind("road", Utils.stringify(road));
        }

        List<Marker> res = q.map((rs, ctx) -> {
                ArrayList tmp = gson.fromJson(rs.getString("coordinates"), ArrayList.class);

                return new Marker(
                        new GeoCoordinates((Double) tmp.get(0), (Double) tmp.get(1)),
                        gson.fromJson(rs.getString("addressNode"), OSMAddressNode.class)
                );
            }
        ).list();

        handler.close();

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
                .map(e-> e.getKey() + " ILIKE :" + e.getKey().toLowerCase())
                .collect(Collectors.toList());

        final String filter = " WHERE " + String.join(" AND ", filters);

        println(filters);

        return filters.isEmpty() ? "" : filter;
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.POST, value = "/add", headers="Content-Type=application/json; charset=utf-8")
    public @ResponseBody RESTResource<Integer> add(@RequestBody String body, Model model) throws IOException {

        GeoCoordinates coordinates = gson.fromJson(body, GeoCoordinates.class);
//
        println(body);

        Integer ret = -1;

        OkHttpClient client = new OkHttpClient();

        Request reverseGeoCoding = new Request.Builder()
                .url("https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=" + coordinates.getLat() + "&lon=" + coordinates.getLng())
                .build();

        Response reverseGeoCodingResult = client.newCall(reverseGeoCoding).execute();

        assert reverseGeoCodingResult.body() != null;
        Matcher matcher = addressRegex.matcher(reverseGeoCodingResult.body().string());

        if (matcher.find()){
            String address = matcher.group(1);

            address = address.replaceFirst("address[0-9]+", "place");
            address = address.replaceFirst("suburb", "town");
            address = address.replaceFirst("village", "neighbourhood");
            address = address.replaceFirst("country_code", "countryCode");
            address = address.replaceFirst("house_number", "houseNumber");
            address = address.replaceFirst("state", "region");

            OSMAddressNode node = gson.fromJson("{" + address + "}", OSMAddressNode.class);

            println(node.toString());

            Handle handler = JdbiSingleton.getInstance().open();

            ret = handler.createUpdate(
                    "INSERT " +
                            "INTO Markers(" +
                                "coordinates, country, country_code, region, county, town, place, postcode, neighbourhood, road, house_number" +
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
                    ).bind("lat", coordinates.getLat())
                    .bind("lng", coordinates.getLng())
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
                    .execute();

            handler.close();
        }

        return new RESTResource<>(counter.incrementAndGet(), ret);
    }
}
