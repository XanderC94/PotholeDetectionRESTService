package core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
@EnableConfigurationProperties
public class Application {

    public static void main(String[] args) throws IOException {

        TokenManager.getInstance(new File("./src/main/resources/token/tokens.json"));

        SpringApplication.run(Application.class, args);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                TokenManager.getInstance().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }
}
