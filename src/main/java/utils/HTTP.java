package utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

public class HTTP {
    public static String get(final URI uri) throws IOException {

        return HTTP.get(uri.toURL());
    }

    public static String get(final String url) throws IOException {

        return HTTP.get(new URL(url));
    }

    public static String get(final URL url) throws IOException {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(url).build();

        Response result = client.newCall(request).execute();

        assert result.body() != null;

        return result.body().string();
    }
}
