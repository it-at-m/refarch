package de.muenchen.refarch.s3.integration.domain.model;

import java.util.Set;
import lombok.Data;

@Data
public class FilesInFolder {

    private Set<String> pathToFiles;

}
