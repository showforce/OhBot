package ohbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by lambertyang on 2017/1/13.
 */
@SpringBootApplication
public class OhBotApplication {
    public static void main(String[] args) throws IOException {
        SpringApplication.run(OhBotApplication.class, args);
    }
}
