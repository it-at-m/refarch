package de.muenchen.refarch.integration.dms.adapter.out.fabasoft;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "refarch.dms.fabasoft")
public class FabasoftProperties {

    @NotBlank
    private String url;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String businessapp;

    @NotBlank
    private String uiUrl;

    @NotNull
    private Boolean enableMtom;
}
