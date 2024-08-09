package de.muenchen.refarch.integration.s3.domain.model;

import java.util.Map;

public record FileSizesInFolder(
        Map<String, Long> fileSizes) {
}
