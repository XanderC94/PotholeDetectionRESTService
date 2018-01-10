package hello;

import org.jdbi.v3.core.Jdbi;

public class JdbiSingleton {

    private static Boolean init = false;
    private static Jdbi jdbiInstance;

    public JdbiSingleton(final Jdbi jdbi) {
        if (!init) {
            jdbiInstance = jdbi;
            init = true;
        }
    }

    public static Jdbi getInstance(){
        return jdbiInstance;
    }
}
