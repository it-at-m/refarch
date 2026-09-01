package de.muenchen.oss.refarch.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@SuppressWarnings("PMD.UseUtilityClass")
public class ApiGatewayApplication {

    /* package */ static void main(final String... args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
