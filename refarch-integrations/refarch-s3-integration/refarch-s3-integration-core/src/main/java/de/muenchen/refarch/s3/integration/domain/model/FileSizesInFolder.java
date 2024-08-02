package de.muenchen.refarch.s3.integration.domain.model;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileSizesInFolder {

    private Map<String, Long> fileSizes;
}
