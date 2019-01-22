package core;

import json.*;
import org.joda.time.DateTime;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import rest.RESTResponse;
import rest.create.DeviceSubscriber;
import rest.create.MarkerInserter;
import rest.read.*;
import rest.update.FeedbackInserter;
import rest.update.UpvoteUpdater;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import static utils.Utils.defaultCountry;
import static utils.Utils.defaultCounty;
import static utils.Utils.defaultRegion;
import static utils.Utils.defaultRoad;
import static utils.Utils.defaultTown;

/**
 *
 *
 */
@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/pothole")
public class RestAPIController {

    private final AtomicLong counter = new AtomicLong();

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "")
    public @ResponseBody
    RESTResponse<List<Marker>> collect(Model model) throws Exception {

        return getResources(defaultCountry, defaultRegion, defaultCounty, defaultTown, defaultRoad, model);

    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/{country}")
    public @ResponseBody
    RESTResponse<List<Marker>> collect(
            @PathVariable(value = "country") String country,
            Model model) throws Exception {

        return getResources(country, defaultRegion, defaultCounty, defaultTown, defaultRoad, model);

    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/{country}/{region}")
    public @ResponseBody
    RESTResponse<List<Marker>> collect(
            @PathVariable(value = "country") String country,
            @PathVariable(value = "region") String region,
            Model model) throws Exception {

        return getResources(country, region, defaultCounty, defaultTown, defaultRoad, model);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/{country}/{region}/{county}")
    public @ResponseBody
    RESTResponse<List<Marker>> collect(
            @PathVariable(value = "country") String country,
            @PathVariable(value = "region") String region,
            @PathVariable(value = "county") String county,
            Model model) throws Exception {

        return getResources(country, region, county, defaultTown, defaultRoad, model);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/{country}/{region}/{county}/{town}")
    public @ResponseBody
    RESTResponse<List<Marker>> collect(
            @PathVariable(value = "country") String country,
            @PathVariable(value = "region") String region,
            @PathVariable(value = "county") String county,
            @PathVariable(value = "town") String town,
            Model model) throws Exception {

        return getResources(country, region, county, town, defaultRoad, model);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/{country}/{region}/{county}/{town}/{road}")
    public @ResponseBody
    RESTResponse<List<Marker>> collect(
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
    public @ResponseBody
    RESTResponse<List<Marker>> road(
            @PathVariable(value = "road_name") String road,
            Model model) throws Exception {

//        Logging.log(road);

        return getResources(defaultCountry, defaultRegion, defaultCounty, defaultTown, road, model);
    }

    private RESTResponse<List<Marker>> getResources(
            String country, String region, String county, String town, String road, Model model
    ) throws Exception {

        List<Marker> res = SelectorByHierarchy.getResources(country, region, county, town, road, model);

        return new RESTResponse<>(counter.incrementAndGet(), res);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/route")
    public @ResponseBody
    RESTResponse<RoutesResponse> route(@RequestParam("from") String from,
                                       @RequestParam("to") String to,
                                       @RequestParam(value = "dist", required = false, defaultValue = "100") Integer dist,
                                       @RequestParam(value = "mode", required = false, defaultValue = "driving-car") String mode,
                                       @RequestParam(value = "route", required = false, defaultValue = "recommended") String route,
                                       Model model) throws Exception {


        RoutesResponse response = RouteSelector.route(from, to, dist, mode, route, model);

        if(response.getMarkers().isEmpty()){
            return new RESTResponse<>(counter.incrementAndGet(), response)
                    .withInfo("{" +
                            "\"info\":\"There is no viable route from "+ from + " to " + to + "\", " +
                            "\"queryOutput\":" + response.getRoutingServiceResponse().asText() +
                            "}");

        } else {

            return new RESTResponse<>(counter.incrementAndGet(), response)
                    .withInfo("Route found.");

        }
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/at")
    public @ResponseBody
    RESTResponse<Marker> getMarkerAt(@RequestParam("coordinates") String point,
                                     Model model) throws Exception {

        return  new RESTResponse<>(counter.incrementAndGet(),
                AreaSelector.getMarkerAt(point, model));
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/area")
    public @ResponseBody
    RESTResponse<List<Marker>> area(@RequestParam("origin") String origin,
                                    @RequestParam("radius") Double radius,
                                    Model model) throws Exception {

        return new RESTResponse<>(counter.incrementAndGet(),
                AreaSelector.getMarkersInArea(origin, radius, model));
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/geodecode")
    public @ResponseBody
    RESTResponse<GeoCoordinates> geodecode(@RequestParam("place") String place, Model model) {

        return new RESTResponse<>(
                counter.incrementAndGet(),
                GeoCoding.decode(place).orElse(GeoCoordinates.empty())
        );
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/reverse")
    public @ResponseBody
    RESTResponse<OSMAddressNode> reverse(
            @RequestParam("coordinates") String coordinates, Model model) throws Exception {

        return new RESTResponse<>(
                counter.incrementAndGet(),
                GeoCoding.reverse(GeoCoordinates.fromString(coordinates))
                        .orElse(OSMAddressNode.empty()).unfiltered()
        );
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.POST, value = "", headers="Content-Type=application/json; charset=utf-8")
    public @ResponseBody
    RESTResponse<String> add(@RequestBody String body, Model model) throws Exception {

        return new RESTResponse<>(counter.incrementAndGet(), MarkerInserter.addMarker(body, model));

    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, value = "/id/{id}", headers="Content-Type=application/json; charset=utf-8")
    public @ResponseBody
    RESTResponse<Marker> getMarkerByUId(@PathVariable Integer id,
                                        Model model) throws Exception {
        Optional<Marker> optionalMarker = SelectorById.getMarkerByUId(id, model);

        if (optionalMarker.isPresent()) {
            return new RESTResponse<>(counter.incrementAndGet(), optionalMarker.get());
        } else {
            String info = String.format("Added no matching marker with id=%d;\n", id);
            return new RESTResponse<>(counter.incrementAndGet(), new Marker()).withInfo(info);
        }
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.POST, value = "/register", headers="Content-Type=application/json; charset=utf-8")
    public @ResponseBody
    RESTResponse<String> addToken(@RequestBody String registration, Model model) throws Exception {

        return new RESTResponse<>(counter.incrementAndGet(), DeviceSubscriber.addToken(registration, model));
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.PUT, value = "/{id}/upvote", headers="Content-Type=application/json; charset=utf-8")
    public @ResponseBody
    RESTResponse<Integer> addUpvote(@PathVariable Integer id,
                                    @RequestBody String body,
                                    Model model) throws Exception {


        String info = String.format("Added Upvote to %d on date %s;\n", id, DateTime.now().toLocalDateTime());

        return new RESTResponse<>(counter.incrementAndGet(), UpvoteUpdater.addUpvote(id, body, model)).withInfo(info);

    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.PUT, value = "/{id}/comment", headers="Content-Type=application/json; charset=utf-8")
    public @ResponseBody
    RESTResponse<Integer> addFeedback(@PathVariable Integer id,
                                      @RequestBody String body,
                                      Model model) throws Exception {

        Tuple<Integer, String> t = FeedbackInserter.addFeedback(id, body, model);

        String info = String.format(
                "Added Comment %s to %d on date %s;\n",
                t.getY(), id, DateTime.now().toLocalDateTime()
        );

        return new RESTResponse<>(counter.incrementAndGet(), t.getX()).withInfo(info);
    }
}
