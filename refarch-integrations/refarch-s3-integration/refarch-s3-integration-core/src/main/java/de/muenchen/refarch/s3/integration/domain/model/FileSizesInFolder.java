package de.muenchen.refarch.s3.integration.domain.model;

import java.util.Map;

public record FileSizesInFolder(
        Map<String, Long> fileSizes) {
}
