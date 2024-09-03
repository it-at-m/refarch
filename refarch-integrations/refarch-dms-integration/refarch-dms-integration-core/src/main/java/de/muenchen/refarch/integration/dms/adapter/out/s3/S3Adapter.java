package de.muenchen.refarch.integration.dms.adapter.out.s3;

import de.muenchen.refarch.integration.dms.application.port.out.TransferContentOutPort;
import de.muenchen.refarch.integration.dms.domain.model.Content;
import de.muenchen.refarch.integration.dms.application.port.out.LoadFileOutPort;
import de.muenchen.oss.digiwf.message.process.api.error.BpmnError;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFileRepository;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFolderRepository;
import de.muenchen.refarch.integration.s3.client.service.FileValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FilenameUtils;
import org.springframework.util.unit.DataSize;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class S3Adapter implements LoadFileOutPort, TransferContentOutPort {

    private static final String LOAD_FOLDER_FAILED = "LOAD_FOLDER_FAILED";
    private static final String FILE_SIZE_ERROR = "FILE_SIZE_ERROR";
    private static final String BATCH_SIZE_ERROR = "BATCH_SIZE_ERROR";

    private final DocumentStorageFileRepository documentStorageFileRepository;
    private final DocumentStorageFolderRepository documentStorageFolderRepository;
    private final FileValidationService fileService;

    @Override
    public List<Content> loadFiles(final List<String> filePaths) {
        validateFileSizes(filePaths);
        final List<Content> contents = new ArrayList<>();
        filePaths.forEach(path -> {
            if (path.endsWith("/")) {
                contents.addAll(getFilesFromFolder(path));
            } else {
                contents.add(getFile(path));
            }
        });
        return contents;
    }

    private void validateFileSizes(final List<String> filePaths) {
        // Collect file sizes along with their paths
        final Map<String, Long> fileSizesWithPaths = getFileSizesWithPaths(filePaths);

        // Filter files exceeding maximum size
        final Map<String, Long> oversizedFiles = fileService.getOversizedFiles(fileSizesWithPaths);
        // Handle oversized files
        if (!oversizedFiles.isEmpty()) {
            final String filesOverMaxString = oversizedFiles.entrySet().stream()
                    .map(entry -> entry.getKey() + ": " + DataSize.ofBytes(entry.getValue()).toMegabytes() + " MB")
                    .collect(Collectors.joining(System.lineSeparator()));
            throw new BpmnError(FILE_SIZE_ERROR,
                    String.format("The following files exceed the maximum size of %d MB:%n%s", fileService.getMaxFileSize().toMegabytes(), filesOverMaxString));
        }

        // Validate total batch size
        final DataSize totalFileSize = fileService.getTotalBatchSize(fileSizesWithPaths);
        if (!fileService.isValidBatchSize(totalFileSize))
            throw new BpmnError(BATCH_SIZE_ERROR, String.format("Batch size of %d MB is too large. Allowed are %d MB.",
                    totalFileSize.toMegabytes(), fileService.getMaxBatchSize().toMegabytes()));
    }

    private Map<String, Long> getFileSizesWithPaths(final List<String> filePaths) {
        return filePaths.stream()
                .flatMap(path -> getFileSizeForPath(path).entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, Long> getFileSizeForPath(final String path) {
        if (path.endsWith("/")) {
            return getSizesInFolderRecursively(path);
        } else {
            return Map.of(path, getFileSize(path));
        }
    }

    private Map<String, Long> getSizesInFolderRecursively(final String folderPath) {
        try {
            return Objects.requireNonNull(documentStorageFolderRepository
                    .getAllFileSizesInFolderRecursively(folderPath));
        } catch (final DocumentStorageException | DocumentStorageServerErrorException | DocumentStorageClientErrorException e) {
            throw new BpmnError(LOAD_FOLDER_FAILED, "Metadata of a folder could not be loaded from url: " + folderPath);
        }
    }

    private long getFileSize(final String filePath) {
        try {
            return Objects.requireNonNull(documentStorageFileRepository
                    .getFileSize(filePath));
        } catch (final DocumentStorageException | DocumentStorageServerErrorException | DocumentStorageClientErrorException e) {
            throw new BpmnError(LOAD_FOLDER_FAILED, "Metadata of a folder could not be loaded from url: " + filePath);
        }
    }

    private List<Content> getFilesFromFolder(final String folderPath) {
        try {
            final List<Content> contents = new ArrayList<>();
            final Set<String> filepath;
            filepath = documentStorageFolderRepository.getAllFilesInFolderRecursively(folderPath);
            if (Objects.isNull(filepath)) throw new BpmnError(LOAD_FOLDER_FAILED, "An folder could not be loaded from url: " + folderPath);
            filepath.forEach(file -> contents.add(getFile(file)));
            return contents;
        } catch (final DocumentStorageException | DocumentStorageServerErrorException | DocumentStorageClientErrorException e) {
            throw new BpmnError(LOAD_FOLDER_FAILED, "An folder could not be loaded from url: " + folderPath);
        }
    }

    private Content getFile(final String filePath) {
        try {
            final byte[] bytes;
            bytes = this.documentStorageFileRepository.getFile(filePath, 3);
            final String mimeType = fileService.detectFileType(bytes);
            final String filename = FilenameUtils.getBaseName(filePath);

            // check if mimeType exists
            if (!fileService.isSupported(mimeType))
                throw new BpmnError("FILE_TYPE_NOT_SUPPORTED", "The type of this file is not supported: " + filePath);

            return new Content(fileService.getFileExtension(mimeType), filename, bytes);
        } catch (final DocumentStorageException | DocumentStorageServerErrorException |
                       DocumentStorageClientErrorException e) {
            throw new BpmnError("LOAD_FILE_FAILED", "An file could not be loaded from url: " + filePath);
        }
    }

    @Override
    public void transferContent(final List<Content> content, final String filepath) {
        for (val file : content) {
            try {
                val fullFilePath = (filepath + "/" + file.getName() + "." + file.getExtension()).replace("//", "/");
                this.documentStorageFileRepository.saveFile(fullFilePath, file.getContent(), 1);
            } catch (Exception e) {
                throw new BpmnError("SAVE_FILE_FAILED", "An file could not be saved to path: " + filepath);
            }
        }
    }
}
