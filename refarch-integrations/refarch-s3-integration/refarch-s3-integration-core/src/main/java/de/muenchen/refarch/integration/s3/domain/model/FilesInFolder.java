package de.muenchen.refarch.integration.s3.domain.model;

import java.util.Collections;
import java.util.Set;

public record FilesInFolder(
        Set<String> pathToFiles) {
    public FilesInFolder {
        pathToFiles = Set.copyOf(pathToFiles);
    }

    @Override
    public Set<String> pathToFiles() {
        return Collections.unmodifiableSet(pathToFiles);
    }
}
