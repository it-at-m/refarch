package de.muenchen.refarch.integration.s3.adapter.in.rest.dto;

import de.muenchen.refarch.integration.s3.domain.model.FileData;
import de.muenchen.refarch.integration.s3.domain.validation.FolderInFilePath;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "File data for requesting a presigned url")
public record FileDataDto(
        @NotEmpty @Size(max = FileData.LENGTH_PATH_TO_FILE) @FolderInFilePath String pathToFile,
        @NotNull @Min(FileData.MIN_EXPIRES_IN_MINUTES) Integer expiresInMinutes) {
}
