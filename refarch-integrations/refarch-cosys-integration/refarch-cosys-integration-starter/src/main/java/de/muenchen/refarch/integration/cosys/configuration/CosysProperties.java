package de.muenchen.refarch.integration.cosys.configuration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "refarch.cosys")
public class CosysProperties {

    @NotNull
    @NestedConfigurationProperty
    private MergeProperties merge;

    @NotBlank
    private String url;

    @Getter
    @Setter
    public static class MergeProperties {

        @NotBlank
        private String datafile;

        @NotBlank
        private String inputLanguage;

        @NotBlank
        private String outputLanguage;

        @NotBlank
        private String keepFields;

    }
}
