package core;

import org.jdbi.v3.core.spi.JdbiPlugin;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.spring4.JdbiFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;
import utils.Logging;

import java.util.Arrays;
import java.util.List;

@Component
@Configuration
public class PostgresConfig {

    private final String urlFormat = "%s:%s://%s:%d/%s";

    private final List<JdbiPlugin> plugins = Arrays.asList(new PostgresPlugin());
    private DBProperties props;

    @Autowired
    PostgresConfig(final DBProperties properties) {
        this.props = properties;
        Logging.log("Config Loaded...");
        Logging.log(this.toString());
    }

    @Bean(name = "db")
    public DriverManagerDataSource getDataSource() {

        Logging.log("Selecting Data source...");

        return new DriverManagerDataSource(
                    String.format(urlFormat,
                            props.getDriver(),
                            props.getType(),
                            props.getHostname(),
                            props.getPort(), props.getName()),
                    props.getUser(),
                    props.getPassword()
                );
    }

    @Bean(name = "plugins")
    public List<JdbiPlugin> getPlugins() {

        Logging.log("Loading Plugins...");

        return plugins;
    }

    @Bean(name = "jdbi")
    public JdbiFactoryBean getJDBIFactory() {
        Logging.log("Generating Jdbi Instance...");
        return new JdbiFactoryBean(getDataSource()).setPlugins(getPlugins());
    }

//    @Bean(name = "service")
//    public JdbiInstanceManager getJdbiConnectorInstance() throws Exception {
//        Logging.log("Wrapping Jdbi Singleton...");
//        return JdbiInstanceManager.setPostgreSQLConfig(this);
//    }

    @Bean(name = "transactionManager")
    public DataSourceTransactionManager getTransactionManager() throws Exception {
        Logging.log("Setting Transaction Manager...");
        return new DataSourceTransactionManager(getDataSource());
    }

    @Override
    public String toString() {
        return props.toString();
    }
}
