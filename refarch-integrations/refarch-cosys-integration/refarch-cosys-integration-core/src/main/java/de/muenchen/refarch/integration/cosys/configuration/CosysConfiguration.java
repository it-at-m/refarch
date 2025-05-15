package de.muenchen.refarch.integration.cosys.configuration;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CosysConfiguration {
    private Map<String, String> mergeOptions;
    private String url;
}
