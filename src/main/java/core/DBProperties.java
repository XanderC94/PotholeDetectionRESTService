package core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="database")
public class DBProperties {

    private String driver;
    private String type;
    private String hostname;
    private Integer port;
    private String name;

    private String user;
    private String password;

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriver() {
        return driver;
    }

    public String getType() {
        return type;
    }

    public String getHostname() {
        return hostname;
    }

    public Integer getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "\n{" +
                "\n\tdriver:" + driver +
                ",\n\ttype:" + type +
                ",\n\thostname:" + hostname +
                ",\n\tport:" + port +
                ",\n\tname:" + name +
                ",\n\tuser:" + user +
                ",\n\tpassword:" + password +
                "\n}";
    }
}
