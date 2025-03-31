package de.muenchen.refarch.integration.s3.adapter.in.rest;

import de.muenchen.refarch.integration.s3.adapter.in.rest.dto.FileDataDto;
import de.muenchen.refarch.integration.s3.adapter.in.rest.dto.FileSizeDto;
import de.muenchen.refarch.integration.s3.adapter.in.rest.dto.PresignedUrlDto;
import de.muenchen.refarch.integration.s3.adapter.in.rest.mapper.FileDataMapper;
import de.muenchen.refarch.integration.s3.adapter.in.rest.mapper.FileSizeMapper;
import de.muenchen.refarch.integration.s3.adapter.in.rest.mapper.PresignedUrlMapper;
import de.muenchen.refarch.integration.s3.application.port.in.FileOperationsInPort;
import de.muenchen.refarch.integration.s3.application.port.in.FileOperationsPresignedUrlInPort;
import de.muenchen.refarch.integration.s3.domain.exception.FileExistenceException;
import de.muenchen.refarch.integration.s3.domain.exception.FileSystemAccessException;
import de.muenchen.refarch.integration.s3.domain.model.FileData;
import de.muenchen.refarch.integration.s3.domain.model.FileSize;
import de.muenchen.refarch.integration.s3.domain.model.PresignedUrl;
import de.muenchen.refarch.integration.s3.domain.validation.FolderInFilePath;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "FileAPI", description = "API to interact with files")
@RequestMapping("/file")
public class FileController {

    private final FileOperationsPresignedUrlInPort fileOperationsPresignedUrl;
    private final FileOperationsInPort fileOperations;
    private final FileDataMapper fileMapper;
    private final PresignedUrlMapper presignedUrlMapper;
    private final FileSizeMapper fileSizeMapper;

    @GetMapping
    @Operation(description = "Creates a presigned URL to fetch the file specified in the parameter from the S3 storage")
    public ResponseEntity<PresignedUrlDto> get(@RequestParam @NotEmpty @Size(max = FileData.LENGTH_PATH_TO_FILE) @FolderInFilePath final String pathToFile,
            @RequestParam @NotNull @Min(FileData.MIN_EXPIRES_IN_MINUTES) final Integer expiresInMinutes) {
        try {
            log.info("Received a request for S3 presigned url to download a file");
            final PresignedUrl fileResponse = this.fileOperationsPresignedUrl.getFile(pathToFile, expiresInMinutes);
            final PresignedUrlDto presignedUrlDto = this.presignedUrlMapper.model2Dto(fileResponse);
            return ResponseEntity.ok(presignedUrlDto);
        } catch (final FileSystemAccessException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), exception);
        } catch (final FileExistenceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
        } catch (final Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @GetMapping("/size")
    @Operation(description = "Retrieves the size of the file specified in the parameter from the S3 storage")
    public ResponseEntity<FileSizeDto> getFileSize(
            @RequestParam @NotEmpty @Size(max = FileSize.LENGTH_PATH_TO_FILE) @FolderInFilePath final String pathToFile) {
        try {
            log.info("Received a request for a file size");
            final FileSize fileSize = this.fileOperations.getFileSize(pathToFile);
            final FileSizeDto fileSizeDto = this.fileSizeMapper.model2dto(fileSize);
            return ResponseEntity.ok(fileSizeDto);
        } catch (final FileSystemAccessException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), exception);
        } catch (final FileExistenceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
        } catch (final Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @PostMapping
    @Operation(description = "Creates a presigned URL to store the file specified in the parameter within the S3 storage")
    public ResponseEntity<PresignedUrlDto> save(@RequestBody @NotNull @Valid final FileDataDto fileData) {
        try {
            log.info("Received a request for S3 presigned url to upload a new file");
            final PresignedUrl presignedUrl = this.fileOperationsPresignedUrl.saveFile(this.fileMapper.dto2Model(fileData));
            final PresignedUrlDto presignedUrlDto = this.presignedUrlMapper.model2Dto(presignedUrl);
            return ResponseEntity.ok(presignedUrlDto);
        } catch (final FileExistenceException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        } catch (final Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), exception);
        }
    }

    @PutMapping
    @Operation(description = "Creates a presigned URL to overwrite the file specified in the parameter within the S3 storage")
    public ResponseEntity<PresignedUrlDto> update(@RequestBody @NotNull @Valid final FileDataDto fileData) {
        try {
            log.info("Received a request for S3 presigned url to upload a existing file");
            final PresignedUrl presignedUrl = this.fileOperationsPresignedUrl.updateFile(this.fileMapper.dto2Model(fileData));
            final PresignedUrlDto presignedUrlDto = this.presignedUrlMapper.model2Dto(presignedUrl);
            return ResponseEntity.ok(presignedUrlDto);
        } catch (final Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), exception);
        }
    }

    @DeleteMapping
    @Operation(description = "Creates a presigned URL to delete the file specified in the parameter from the S3 storage")
    public ResponseEntity<PresignedUrlDto> delete(
            @RequestParam @NotEmpty @Size(max = FileData.LENGTH_PATH_TO_FILE) @FolderInFilePath final String pathToFile,
            @RequestParam @NotNull @Min(FileData.MIN_EXPIRES_IN_MINUTES) final Integer expiresInMinutes) {
        try {
            log.info("Received a request for S3 presigned url to delete a file");
            final PresignedUrl presignedUrl = this.fileOperationsPresignedUrl.deleteFile(pathToFile, expiresInMinutes);
            final PresignedUrlDto presignedUrlDto = this.presignedUrlMapper.model2Dto(presignedUrl);
            return ResponseEntity.ok(presignedUrlDto);
        } catch (final FileSystemAccessException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), exception);
        } catch (final FileExistenceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
        } catch (final Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

}
