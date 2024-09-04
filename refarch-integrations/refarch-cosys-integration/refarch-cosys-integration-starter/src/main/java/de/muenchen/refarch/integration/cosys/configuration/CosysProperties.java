package de.muenchen.refarch.integration.cosys.configuration;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "io.muenchendigital.digiwf.cosys")
public class CosysProperties {

    @NestedConfigurationProperty
    private MergeProperties merge;

    @NotBlank
    private String url;

    @Getter
    @Setter
    public static class MergeProperties {

        private String datafile;

        private String inputLanguage;

        private String outputLanguage;

        private String keepFields;

    }
}
