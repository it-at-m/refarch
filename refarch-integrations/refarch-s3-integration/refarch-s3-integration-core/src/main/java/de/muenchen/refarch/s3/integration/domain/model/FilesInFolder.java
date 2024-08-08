package de.muenchen.refarch.s3.integration.domain.model;

import java.util.Set;

public record FilesInFolder(
        Set<String> pathToFiles) {
}
