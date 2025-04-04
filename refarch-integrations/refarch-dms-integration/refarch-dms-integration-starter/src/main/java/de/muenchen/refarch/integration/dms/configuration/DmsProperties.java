package de.muenchen.refarch.integration.dms.configuration;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "refarch.dms")
public class DmsProperties {

    /**
     * Supported extensions.
     */
    @NotBlank
    private Map<String, String> supportedFileExtensions;

}
