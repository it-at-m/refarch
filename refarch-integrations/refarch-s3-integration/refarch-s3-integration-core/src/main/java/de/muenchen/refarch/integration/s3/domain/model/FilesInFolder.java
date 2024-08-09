package de.muenchen.refarch.integration.s3.domain.model;

import java.util.Set;

public record FilesInFolder(
        Set<String> pathToFiles) {
}
