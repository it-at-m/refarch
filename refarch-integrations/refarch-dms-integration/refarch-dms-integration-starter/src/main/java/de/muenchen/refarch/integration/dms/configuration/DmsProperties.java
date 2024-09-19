package de.muenchen.refarch.integration.dms.configuration;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
@ConfigurationProperties(prefix = "refarch.dms")
public class DmsProperties {

    /**
     * Supported extensions.
     */
    @NotBlank
    private Map<String, String> supportedFileExtensions;

}
