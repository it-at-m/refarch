package de.muenchen.refarch.integration.s3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Application class for starting the micro-service.
 */
@SpringBootApplication
@EnableScheduling
@SuppressWarnings("PMD.UseUtilityClass")
public class S3IntegrationApplication {
    public static void main(final String[] args) {
        SpringApplication.run(S3IntegrationApplication.class, args);
    }

}
