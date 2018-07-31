package core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import json.Marker;
import json.OSMAddressNode;
import json.GeoCoordinates;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.Query;
import org.springframework.web.bind.annotation.*;
import rest.RESTResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
public class RestAPIController {

    private static final Pattern regex = Pattern.compile("(?:\"address\":)\\{(.*?)\\}");
    private static final Gson gson = new GsonBuilder().create();

    private final String areaDefault = "Any";
    private final AtomicLong counter = new AtomicLong();

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/collect")
    public RESTResource<List<GeoCoordinates>> collect(@RequestParam(value = "area", defaultValue = areaDefault) String area) {

        Handle handler = JdbiSingleton.getInstance().open();

        Query q = handler.select(
                "SELECT " +
                        "json_build_object(" +
                            "'countryCode', country_code, " +
                            "'region', region, " +
                            "'county', county, " +
                            "'town', town, " +
                            "'place', place, " +
                            "'neighbourhood', neighbourhood, " +
                            "'road', road" +
                        ") as AddressNode, " +
                        "ST_AsGeoJSON(Coordinates)::json->'coordinates' AS Coordinates " +
                    "FROM markers" + (area.equals(areaDefault) ? "" : " WHERE City ILIKE :area OR County ILIKE :area")
        );

        if (!area.equals(areaDefault)) {
            q = q.bind("area", area);
        }

        List<Marker> res = q.map((rs, ctx) -> {

                    ArrayList tmp = gson.fromJson(rs.getString("Coordinates"), ArrayList.class);

                    return new Marker(
                            new GeoCoordinates((Double) tmp.get(0), (Double) tmp.get(1)),
                            gson.fromJson(rs.getString("AddressNode"), OSMAddressNode.class)
                    );
                }
        ).list();

        handler.close();

        return new RESTResource<>(counter.incrementAndGet(),
                res.stream().map(m-> m.coordinates).collect(Collectors.toList()));
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.POST, value = "/add", headers="Content-Type=application/json")
    public RESTResource<Integer> add(@RequestBody String body) throws IOException {

        GeoCoordinates coordinates = gson.fromJson(body, GeoCoordinates.class);
//
        System.out.println(body);

        Integer ret = -1;

        OkHttpClient client = new OkHttpClient();

        Request reverseGeoCoding = new Request.Builder()
                .url("https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=" + coordinates.lat + "&lon=" + coordinates.lng)
                .build();

        Response reverseGeoCodingResult = client.newCall(reverseGeoCoding).execute();

        assert reverseGeoCodingResult.body() != null;
        Matcher matcher = regex.matcher(reverseGeoCodingResult.body().string());

        if (matcher.find()){
            String address = matcher.group(1);

            address = address.replaceFirst("address[0-9]+", "place");
            address = address.replaceFirst("suburb", "town");
            address = address.replaceFirst("village", "neighbourhood");
            address = address.replaceFirst("country_code", "countryCode");
            address = address.replaceFirst("house_number", "houseNumber");
            address = address.replaceFirst("state", "region");

            OSMAddressNode node = gson.fromJson("{" + address + "}", OSMAddressNode.class);

            System.out.println(node.toString());

            Handle handler = JdbiSingleton.getInstance().open();

            ret = handler.createUpdate(
                    "INSERT " +
                            "INTO Markers(" +
                                "coordinates, country, country_code, region, county, town, place, postcode, neighbourhood, road, house_number" +
                            ") " +
                            "VALUES (" +
                            "ST_SetSRID(ST_MakePoint(:lat, :lng), 4326)," +
                            ":Country, :Country_Code, :Region, :County," +
                            ":Town, :Place, :Postcode, :Neighbourhood, :Road, :House_Number" +
                            ");"
            ).bind("lat", coordinates.lat).bind("lng", coordinates.lng)
                    .bind("Country", node.getCountry())
                    .bind("Country_Code", node.getCountryCode())
                    .bind("Region", node.getRegion())
                    .bind("County", node.getCounty())
                    .bind("Town", node.getTown())
                    .bind("Place", node.getPlace())
                    .bind("Postcode", node.getPostcode())
                    .bind("Neighbourhood", node.getNeighbourhood())
                    .bind("Road", node.getRoad())
                    .bind("House_Number", node.getHouseNumber())
                    .execute();

            handler.close();
        }

        return new RESTResource<>(counter.incrementAndGet(), ret);
    }
}
