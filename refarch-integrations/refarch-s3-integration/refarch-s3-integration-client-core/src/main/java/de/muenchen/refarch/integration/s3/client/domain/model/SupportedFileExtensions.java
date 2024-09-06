package de.muenchen.refarch.integration.s3.client.domain.model;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import lombok.NoArgsConstructor;

/**
 * Map of allowed file extensions in the format "extension (key): mime-type (value)".
 * I.e. "pdf: application/pdf"
 */
@NoArgsConstructor
public class SupportedFileExtensions extends HashMap<String, String> {
    @Serial
    private static final long serialVersionUID = 1L;
    public SupportedFileExtensions(final Map<String, String> map) {
        super(map);
    }
}
