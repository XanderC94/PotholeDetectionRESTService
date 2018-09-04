package utils;

import java.util.regex.Pattern;

public class Regex {

    public static final Pattern reverseGeocodingAddress =
            Pattern.compile("(?:\"address\":)\\{(.*?)\\}");

    public static final Pattern geocodingCoordinates =
            Pattern.compile("\"lat\":\"[+-]?\\d*\\.\\d*\",\"lon\":\"[+-]?\\d*\\.\\d*\"");

    public static final Pattern routeGeometry =
            Pattern.compile("(?:\"geometry\":\\s?)\\{\\s?(.*?)\\s?\\}");

    //    public static final Pattern arrayRegex = Pattern.compile("([+-]?\\d*\\.\\d*)\\s?([N|E]?)");
    public static final Pattern floatingPoint = Pattern.compile("[+-]?\\d+\\.?\\d+");

    public static final Pattern coordinatesTuple =
            Pattern.compile("\\[?\\s?("+floatingPoint.pattern()+")\\s?([N|E]?)\\s?," +
                    "\\s?("+floatingPoint.pattern()+")\\s?([N|E]?)\\s?\\]?");
}
