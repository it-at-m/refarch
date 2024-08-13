package de.muenchen.refarch.email.integration.adapter.out.s3;

import de.muenchen.refarch.email.integration.application.port.out.LoadMailAttachmentOutPort;
import de.muenchen.refarch.email.integration.domain.exception.LoadAttachmentError;
import de.muenchen.refarch.email.model.FileAttachment;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFileRepository;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFolderRepository;
import de.muenchen.refarch.integration.s3.client.service.FileValidationService;
import jakarta.mail.util.ByteArrayDataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

@Slf4j
@RequiredArgsConstructor
public class S3Adapter implements LoadMailAttachmentOutPort {

    private final DocumentStorageFileRepository documentStorageFileRepository;
    private final DocumentStorageFolderRepository documentStorageFolderRepository;
    private final FileValidationService fileValidationService;

    @Override
    public List<FileAttachment> loadAttachments(final List<String> filePaths) {
        final List<FileAttachment> attachments = new ArrayList<>();
        filePaths.forEach(path -> {
            if (path.endsWith("/")) {
                attachments.addAll(getFilesFromFolder(path));
            } else {
                attachments.add(getFile(path));
            }
        });
        return attachments;
    }

    private List<FileAttachment> getFilesFromFolder(final String folderPath) {
        try {
            final List<FileAttachment> contents = new ArrayList<>();
            final Set<String> filepath;
            filepath = documentStorageFolderRepository.getAllFilesInFolderRecursively(folderPath);
            if (Objects.isNull(filepath))
                throw new LoadAttachmentError("An folder could not be loaded from url: " + folderPath);
            filepath.forEach(file -> contents.add(getFile(file)));
            return contents;
        } catch (final DocumentStorageException | DocumentStorageServerErrorException | DocumentStorageClientErrorException e) {
            throw new LoadAttachmentError("An folder could not be loaded from path: " + folderPath);
        }
    }

    private FileAttachment getFile(final String filePath) {
        try {
            final byte[] bytes;
            bytes = this.documentStorageFileRepository.getFile(filePath, 3);
            final String mimeType = fileValidationService.detectFileType(bytes);
            final String filename = FilenameUtils.getName(filePath);
            final ByteArrayDataSource file = new ByteArrayDataSource(bytes, mimeType);

            // check if mimeType exists
            if (!fileValidationService.isSupported(mimeType))
                throw new LoadAttachmentError("The type of this file is not supported: " + filePath);

            return new FileAttachment(filename, file);
        } catch (final DocumentStorageException | DocumentStorageServerErrorException | DocumentStorageClientErrorException e) {
            throw new LoadAttachmentError("An file could not be loaded from path: " + filePath);
        }
    }
}
