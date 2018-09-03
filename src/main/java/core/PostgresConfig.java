package core;

import org.jdbi.v3.core.spi.JdbiPlugin;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.spring4.JdbiFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import utils.Utils;

import java.util.Arrays;
import java.util.List;

@Configuration
public class PostgresConfig {

    private final String urlFormat = "%s:%s://%s:%d/%s";

    private final List<JdbiPlugin> plugins = Arrays.asList(new PostgresPlugin());
    private DBProperties props;

    @Autowired
    PostgresConfig(final DBProperties properties) {
        this.props = properties;
        Utils.log("Config Loaded...");
        Utils.log(this.toString());
    }

    @Bean(name = "db")
    public DriverManagerDataSource getDataSource() {

        Utils.log("Selecting Data source...");

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

        Utils.log("Loading Plugins...");

        return plugins;
    }

    @Bean(name = "jdbi")
    public JdbiFactoryBean getJDBIFactory() {
        Utils.log("Generating Jdbi Instance...");
        return new JdbiFactoryBean(getDataSource()).setPlugins(getPlugins());
    }

    @Bean(name = "service")
    public JdbiSingleton getJdbiConnectorInstance() throws Exception {
        Utils.log("Wrapping Jdbi Singleton...");
        return new JdbiSingleton(getJDBIFactory().getObject());
    }

    @Bean(name = "transactionManager")
    public DataSourceTransactionManager getTransactionManager() throws Exception {
        Utils.log("Setting Transaction Manager...");
        return new DataSourceTransactionManager(getDataSource());
    }

    @Override
    public String toString() {
        return props.toString();
    }
}
