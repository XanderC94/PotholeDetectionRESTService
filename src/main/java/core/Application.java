package core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import utils.Utils;

@SpringBootApplication
@EnableConfigurationProperties
public class Application {

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);
    }
}
