package de.muenchen.refarch.integration.s3.client.domain.model;

import java.util.HashMap;
import java.util.Map;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SupportedFileExtensions extends HashMap<String, String> {
    public SupportedFileExtensions(final Map<String, String> map) {
        super(map);
    }
}
