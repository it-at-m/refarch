package de.muenchen.refarch.s3.integration.rest;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("refarch.s3")
public class RestProperties {
    private String apiBaseUrl;
}
