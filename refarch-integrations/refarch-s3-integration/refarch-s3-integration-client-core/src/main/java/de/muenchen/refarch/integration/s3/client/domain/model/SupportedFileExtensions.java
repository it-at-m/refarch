package de.muenchen.refarch.integration.s3.client.domain.model;

import java.util.HashMap;
import java.util.Map;
import lombok.NoArgsConstructor;

/**
 * Map of allowed file extensions in the format "extension (key): mime-type (value)".
 * I.e. "pdf: application/pdf"
 */
@NoArgsConstructor
@SuppressWarnings("PMD.MissingSerialVersionUID")
public class SupportedFileExtensions extends HashMap<String, String> {
    public SupportedFileExtensions(final Map<String, String> map) {
        super(map);
    }
}
