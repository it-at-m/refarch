package de.muenchen.refarch.integration.dms.example;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("refarch.dms.example")
@Getter
@Setter
@Validated
public class DmsExampleProperties {
    @NotBlank
    private String username;
    @NotBlank
    private String targetProcedureCoo;
}
