package de.muenchen.refarch.integration.s3.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for integrating with an S3-compatible object storage.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "refarch.s3")
public class S3IntegrationProperties {

    /**
     * Endpoint URL of the S3-compatible service (e.g., https://s3.amazonaws.com or
     * http://localhost:9000).
     * Must not be blank.
     */
    @NotBlank private String url;

    /**
     * Region to use for the S3 client and presigner. For S3-compatible services like MinIO,
     * this is often "us-east-1" and the default.
     */
    @NotBlank private String region = "us-east-1";

    /**
     * Access key used for authentication with the S3-compatible service.
     * Must not be blank.
     */
    @NotBlank private String accessKey;

    /**
     * Secret key used for authentication with the S3-compatible service.
     * Must not be blank. Avoid logging or exposing this value.
     */
    @NotBlank private String secretKey;

    /**
     * Whether to use path-style access (e.g., http://endpoint/bucket/object) instead of
     * virtual-hosted-style access (e.g., http://bucket.endpoint/object). Defaults to {@code true}.
     */
    private boolean pathStyleAccessEnabled = true;

    /**
     * Whether to perform a connectivity check to the configured S3 endpoint during application startup.
     * Defaults to {@code true}. Must not be {@code null}.
     */
    private boolean initialConnectionTest = true;
}
