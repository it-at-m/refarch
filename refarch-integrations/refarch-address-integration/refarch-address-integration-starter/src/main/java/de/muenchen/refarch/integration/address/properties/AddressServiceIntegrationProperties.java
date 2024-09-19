package de.muenchen.refarch.integration.address.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "refarch.address")
public class AddressServiceIntegrationProperties {

    @NotBlank
    private String url;

}
