package de.muenchen.refarch.integration.dms.adapter.out.s3;

import de.muenchen.refarch.integration.dms.application.port.out.TransferContentOutPort;
import de.muenchen.refarch.integration.dms.domain.model.Content;
import de.muenchen.refarch.integration.dms.application.port.out.LoadFileOutPort;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.client.exception.FileSizeValidationException;
import de.muenchen.refarch.integration.s3.client.exception.FileTypeValidationException;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFileRepository;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFolderRepository;
import de.muenchen.refarch.integration.s3.client.service.FileValidationService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.util.unit.DataSize;

import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class S3Adapter implements LoadFileOutPort, TransferContentOutPort {

    private final DocumentStorageFileRepository documentStorageFileRepository;
    private final DocumentStorageFolderRepository documentStorageFolderRepository;
    private final FileValidationService fileService;

    @Override
    public List<Content> loadFiles(final List<String> filePaths) throws DocumentStorageException {
        validateFileSizes(filePaths);
        final List<Content> contents = new ArrayList<>();
        for (final String path : filePaths) {
            if (path.endsWith("/")) {
                contents.addAll(getFilesFromFolder(path));
            } else {
                contents.add(getFile(path));
            }
        }
        return contents;
    }

    private void validateFileSizes(final List<String> filePaths) throws DocumentStorageException {
        // Collect file sizes along with their paths
        final Map<String, Long> fileSizesWithPaths = getFileSizesWithPaths(filePaths);

        // Filter files exceeding maximum size
        final Map<String, Long> oversizedFiles = fileService.getOversizedFiles(fileSizesWithPaths);
        // Handle oversized files
        if (!oversizedFiles.isEmpty()) {
            final String filesOverMaxString = oversizedFiles.entrySet().stream()
                    .map(entry -> entry.getKey() + ": " + DataSize.ofBytes(entry.getValue()).toMegabytes() + " MB")
                    .collect(Collectors.joining(System.lineSeparator()));
            throw new FileSizeValidationException(
                    String.format("The following files exceed the maximum size of %d MB:%n%s", fileService.getMaxFileSize().toMegabytes(), filesOverMaxString));
        }

        // Validate total batch size
        final DataSize totalFileSize = fileService.getTotalBatchSize(fileSizesWithPaths);
        if (!fileService.isValidBatchSize(totalFileSize)) {
            throw new FileSizeValidationException(String.format("Batch size of %d MB is too large. Allowed are %d MB.",
                    totalFileSize.toMegabytes(), fileService.getMaxBatchSize().toMegabytes()));
        }
    }

    private Map<String, Long> getFileSizesWithPaths(final List<String> filePaths) throws DocumentStorageException {
        final Map<String, Long> map = new HashMap<>();
        for (final String path : filePaths) {
            for (final Map.Entry<String, Long> stringLongEntry : getFileSizeForPath(path).entrySet()) {
                if (map.put(stringLongEntry.getKey(), stringLongEntry.getValue()) != null) {
                    throw new IllegalStateException("Duplicate key");
                }
            }
        }
        return map;
    }

    private Map<String, Long> getFileSizeForPath(final String path) throws DocumentStorageException {
        if (path.endsWith("/")) {
            return getSizesInFolderRecursively(path);
        } else {
            return Map.of(path, getFileSize(path));
        }
    }

    private Map<String, Long> getSizesInFolderRecursively(final String folderPath) throws DocumentStorageException {
        try {
            return Objects.requireNonNull(documentStorageFolderRepository
                    .getAllFileSizesInFolderRecursively(folderPath));
        } catch (final DocumentStorageServerErrorException | DocumentStorageClientErrorException e) {
            throw new DocumentStorageException("Metadata of a folder could not be loaded from url: " + folderPath, e);
        }
    }

    private long getFileSize(final String filePath) throws DocumentStorageException {
        try {
            return Objects.requireNonNull(documentStorageFileRepository
                    .getFileSize(filePath));
        } catch (final DocumentStorageServerErrorException | DocumentStorageClientErrorException e) {
            throw new DocumentStorageException("Metadata of a folder could not be loaded from url: " + filePath, e);
        }
    }

    private List<Content> getFilesFromFolder(final String folderPath) throws DocumentStorageException {
        try {
            final List<Content> contents = new ArrayList<>();
            final Set<String> filepath;
            filepath = documentStorageFolderRepository.getAllFilesInFolderRecursively(folderPath);
            if (Objects.isNull(filepath)) {
                throw new DocumentStorageException("An folder could not be loaded from url: " + folderPath);
            }
            for (final String file : filepath) {
                contents.add(getFile(file));
            }
            return contents;
        } catch (final DocumentStorageServerErrorException | DocumentStorageClientErrorException e) {
            throw new DocumentStorageException("An folder could not be loaded from url: " + folderPath, e);
        }
    }

    private Content getFile(final String filePath) throws DocumentStorageException {
        try {
            final byte[] bytes;
            bytes = this.documentStorageFileRepository.getFile(filePath, 3);
            final String mimeType = fileService.detectFileType(bytes);
            final String filename = FilenameUtils.getBaseName(filePath);

            if (!fileService.isSupported(mimeType)) {
                throw new FileTypeValidationException("The type of this file is not supported: " + filePath);
            }

            return new Content(fileService.getFileExtension(mimeType), filename, bytes);
        } catch (final DocumentStorageServerErrorException | DocumentStorageClientErrorException e) {
            throw new DocumentStorageException("An file could not be loaded from url: " + filePath, e);
        }
    }

    @Override
    public void transferContent(final List<Content> content, final String filepath) throws DocumentStorageException {
        for (final Content file : content) {
            try {
                final String fullFilePath = (filepath + "/" + file.name() + "." + file.extension()).replace("//", "/");
                this.documentStorageFileRepository.saveFile(fullFilePath, file.content(), 1);
            } catch (final Exception e) {
                throw new DocumentStorageException("An file could not be saved to path: " + filepath, e);
            }
        }
    }
}
