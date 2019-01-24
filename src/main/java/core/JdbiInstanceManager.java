package core;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.spring4.JdbiFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class JdbiInstanceManager {

    private static JdbiInstanceManager singleton;
    private final JdbiFactoryBean jdbiFactory;

    @Autowired
    public JdbiInstanceManager(final JdbiFactoryBean jdbiFactory) {

        this.jdbiFactory = jdbiFactory;
        singleton = this;
    }

    @Bean("service")
    public Jdbi getConnector() throws Exception {
        return singleton.jdbiFactory.getObject();
    }

    public static JdbiInstanceManager getInstance() {
        return JdbiInstanceManager.singleton;
    }
}
