package de.muenchen.refarch.email.integration.adapter.out.s3;

import de.muenchen.refarch.email.integration.application.port.out.LoadMailAttachmentOutPort;
import de.muenchen.refarch.email.integration.domain.exception.LoadAttachmentError;
import de.muenchen.refarch.email.model.FileAttachment;
import de.muenchen.refarch.s3.integration.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.s3.integration.client.exception.DocumentStorageException;
import de.muenchen.refarch.s3.integration.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.s3.integration.client.repository.DocumentStorageFileRepository;
import de.muenchen.refarch.s3.integration.client.repository.DocumentStorageFolderRepository;
import de.muenchen.refarch.s3.integration.client.repository.transfer.S3FileTransferRepository;
import de.muenchen.refarch.s3.integration.client.service.FileService;
import de.muenchen.refarch.s3.integration.client.service.S3StorageUrlProvider;
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

    private final S3FileTransferRepository s3FileTransferRepository;
    private final DocumentStorageFileRepository documentStorageFileRepository;
    private final DocumentStorageFolderRepository documentStorageFolderRepository;
    private final FileService fileService;
    private final S3StorageUrlProvider s3DomainService;

    @Override
    public List<FileAttachment> loadAttachments(final String fileContext, final List<String> filePaths) {
        final List<FileAttachment> attachments = new ArrayList<>();
        filePaths.forEach(path -> {
            final String fullPath = fileContext + "/" + path;
            if (fullPath.endsWith("/")) {
                attachments.addAll(getFilesFromFolder(fullPath));
            } else {
                attachments.add(getFile(fullPath));
            }
        });
        return attachments;
    }

    private List<FileAttachment> getFilesFromFolder(final String folderPath) {
        try {
            final List<FileAttachment> contents = new ArrayList<>();
            final Set<String> filepath;
            filepath = documentStorageFolderRepository.getAllFilesInFolderRecursively(folderPath, s3DomainService.getDefaultDocumentStorageUrl()).block();
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
            bytes = this.documentStorageFileRepository.getFile(filePath, 3, s3DomainService.getDefaultDocumentStorageUrl());
            final String mimeType = fileService.detectFileType(bytes);
            final String filename = FilenameUtils.getName(filePath);
            final ByteArrayDataSource file = new ByteArrayDataSource(bytes, mimeType);

            // check if mimeType exists
            if (!fileService.isSupported(mimeType))
                throw new LoadAttachmentError("The type of this file is not supported: " + filePath);

            return new FileAttachment(filename, file);
        } catch (final DocumentStorageException | DocumentStorageServerErrorException | DocumentStorageClientErrorException e) {
            throw new LoadAttachmentError("An file could not be loaded from path: " + filePath);
        }
    }
}
