package de.muenchen.oss.refarch.integration.pscd.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@SuppressWarnings("PMD.UseUtilityClass")
public class PscdExampleApplication {

    public static void main(final String[] args) {
        SpringApplication.run(PscdExampleApplication.class, args);
    }

}
