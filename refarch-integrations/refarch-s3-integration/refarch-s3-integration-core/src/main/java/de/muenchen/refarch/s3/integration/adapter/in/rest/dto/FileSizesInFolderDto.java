package de.muenchen.refarch.s3.integration.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Data;

@Data
@Schema(description = "File paths to sizes")
public class FileSizesInFolderDto {

    private Map<String, Long> fileSizes;
}
