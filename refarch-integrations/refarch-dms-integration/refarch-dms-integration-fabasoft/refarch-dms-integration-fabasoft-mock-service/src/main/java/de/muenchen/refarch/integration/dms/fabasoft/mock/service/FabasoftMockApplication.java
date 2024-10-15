package de.muenchen.refarch.integration.dms.fabasoft.mock.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@SuppressWarnings("PMD.UseUtilityClass")
public class FabasoftMockApplication {

    public static void main(final String[] args) {
        SpringApplication.run(FabasoftMockApplication.class, args);
    }

}
