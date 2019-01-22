package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.stream.Collectors;

public class Logging {
    final static Logger logger = LoggerFactory.getLogger("REST-Logger");

    public static void println(String x) { System.out.println(x); }

    public static <T extends Object> void println(Collection<T> x) {
        System.out.println(
                String.join(",",
                        x.stream().map(Object::toString).collect(Collectors.toList())
                )
        );
    }

    public static void log(String s) {
        logger.info(s);
    }

    public static <T extends Object> void log(Collection<T> x) {
        final String str = String.join(",",
                x.stream().map(Object::toString).collect(Collectors.toList())
        );

        logger.info(str);
    }
}
