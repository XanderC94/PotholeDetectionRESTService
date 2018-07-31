package hello;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import json.Marker;
import json.OSMAddressNode;
import json.Point;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.Query;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.web.bind.annotation.*;
import rest.RESTResource;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
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
    public RESTResource<Integer> add(@RequestParam(value = "lat") Double lat,
                                     @RequestParam(value = "lng") Double lng) throws IOException {

        String json = "";
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=" + 44 + "&lon=" + 11)
                .build();

        Response response = client.newCall(request).execute();

//        System.out.println(response.body().string());

        assert response.body() != null;
        Matcher matcher = regex.matcher(response.body().string());

        if (matcher.find()){
            String address = matcher.group(1);

            address = address.replaceFirst("address[0-9]+", "place");
            address = address.replaceFirst("suburb", "town");
            address = address.replaceFirst("village", "neighbourhood");
            address = address.replaceFirst("country_code", "countryCode");
            address = address.replaceFirst("house_number", "houseNumber");

            System.out.println(address);

            OSMAddressNode node = gson.fromJson("{" + address + "}", OSMAddressNode.class);
        }

        Handle handler = JdbiSingleton.getInstance().open();

        Integer ret = handler.createUpdate(
                "INSERT " +
                        "INTO Markers(Coordinates) " +
                        "VALUES (" +
                            "ST_SetSRID(ST_MakePoint(:lat, :lng), 4326)" +
                        ");"
        ).bind("lat", lat).bind("lng", lng).execute();

        handler.close();

        return new RESTResource<>(counter.incrementAndGet(), ret);
    }
}
