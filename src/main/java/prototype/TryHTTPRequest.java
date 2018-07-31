package prototype;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import json.OSMAddressNode;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TryHTTPRequest {

    static final Pattern regex = Pattern.compile("(?:\"address\":)\\{(.*?)\\}");
    static final Gson gson = new GsonBuilder().create();

    public static void main(String[] args) throws IOException {
        String json = "";
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=" + 44 + "&lon=" + 12)
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

            System.out.println(node.toString());
        }

    }
}
