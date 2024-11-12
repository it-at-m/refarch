package de.muenchen.refarch.integration.cosys.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication()
@SuppressWarnings("PMD.UseUtilityClass")
public class CosysExampleApplication {

    public static void main(final String[] args) {
        SpringApplication.run(CosysExampleApplication.class, args);
    }

}
