package de.muenchen.refarch.integration.s3.domain.model;

import java.util.Collections;
import java.util.Map;

public record FileSizesInFolder(
        Map<String, Long> fileSizes) {
    public FileSizesInFolder {
        fileSizes = Map.copyOf(fileSizes);
    }

    @Override
    public Map<String, Long> fileSizes() {
        return Collections.unmodifiableMap(fileSizes);
    }
}
