package rest.read;

import json.GeoCoordinates;
import json.OSMAddressNode;
import utils.HTTP;
import utils.Logging;
import utils.Utils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Optional;
import java.util.regex.Matcher;

import static utils.Regex.geocodingCoordinates;
import static utils.Regex.reverseGeocodingAddress;

public class GeoCoding {

    private static final String UTF_8 = java.nio.charset.StandardCharsets.UTF_8.name();

    public static Optional<GeoCoordinates> decode(final String place) {

        final String bodyCache;

        try {

            final String url = String.format(Utils.geoCodingURLFormat, URLEncoder.encode(place, UTF_8), "jsonv2", 1);

            Logging.log(url);

            bodyCache = HTTP.get(url);

        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }

        Matcher matcher = geocodingCoordinates.matcher(bodyCache);

        if (matcher.find()) {
            String coordinates = matcher.group(0).replaceFirst("lon", "lng");

            return Optional.of(Utils.gson.fromJson("{" + coordinates + "}", GeoCoordinates.class));
        } else {
            return Optional.empty();
        }
    }

    public static Optional<OSMAddressNode> reverse(final GeoCoordinates coordinates) {

        final String bodyCache;
        try {

            final String url = String.format(
                    Utils.reverseGeoCodingURLFormat,
                    coordinates.getLng(), coordinates.getLat(),
                    "jsonv2"
            );

            Logging.log(url);

            bodyCache = HTTP.get(url);

        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }

        Matcher matcher = reverseGeocodingAddress.matcher(bodyCache);

        if (matcher.find()) {
            String address = matcher.group(1);

            address = address.replaceFirst("address[0-9]+", "place")
                    .replaceFirst("country_code", "countryCode")
                    .replaceFirst("house_number", "houseNumber")
                    .replaceFirst("state", "region")
                    .replaceFirst("city_district", "district");

            return Optional.of(Utils.gson.fromJson("{" + address + "}", OSMAddressNode.class));
        } else {
            return Optional.empty();
        }
    }
}
