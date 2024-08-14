package de.muenchen.refarch.integration.s3.client.repository.presignedurl;

import de.muenchen.refarch.integration.s3.application.port.in.FileOperationsPresignedUrlInPort;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.domain.exception.FileSystemAccessException;
import de.muenchen.refarch.integration.s3.domain.model.FileData;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PresignedUrlJavaRepository implements PresignedUrlRepository {
    private final FileOperationsPresignedUrlInPort fileOperationsPresignedUrlInPort;

    @Override
    public String getPresignedUrlGetFile(String pathToFile, int expireInMinutes)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            return fileOperationsPresignedUrlInPort.getFile(pathToFile, expireInMinutes).url();
        } catch (FileSystemAccessException e) {
            throw new DocumentStorageException(e.getMessage(), e);
        }
    }

    @Override
    public String getPresignedUrlSaveFile(String pathToFile, int expireInMinutes)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            return fileOperationsPresignedUrlInPort.saveFile(new FileData(pathToFile, expireInMinutes)).url();
        } catch (FileSystemAccessException e) {
            throw new DocumentStorageException(e.getMessage(), e);
        }
    }

    @Override
    public String getPresignedUrlUpdateFile(String pathToFile, int expireInMinutes)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            return fileOperationsPresignedUrlInPort.updateFile(new FileData(pathToFile, expireInMinutes)).url();
        } catch (FileSystemAccessException e) {
            throw new DocumentStorageException(e.getMessage(), e);
        }
    }

    @Override
    public String getPresignedUrlDeleteFile(String pathToFile, int expireInMinutes)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            return fileOperationsPresignedUrlInPort.deleteFile(pathToFile, expireInMinutes).url();
        } catch (FileSystemAccessException e) {
            throw new DocumentStorageException(e.getMessage(), e);
        }
    }
}
