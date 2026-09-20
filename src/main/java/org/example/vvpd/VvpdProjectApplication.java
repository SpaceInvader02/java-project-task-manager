package org.example.vvpd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VvpdProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(VvpdProjectApplication.class, args);
    }
}
