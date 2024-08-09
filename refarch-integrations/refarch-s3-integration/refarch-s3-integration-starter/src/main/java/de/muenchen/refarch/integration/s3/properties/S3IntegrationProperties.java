package de.muenchen.refarch.integration.s3.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "refarch.s3")
public class S3IntegrationProperties {

    @NotBlank
    private String url;

    @NotBlank
    private String accessKey;

    @NotBlank
    private String secretKey;

    @NotBlank
    private String bucketName;

    private Boolean initialConnectionTest;

    private int presignedUrlExpiresInMinutes = 10080; // 7 days
}
