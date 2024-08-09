package de.muenchen.refarch.integration.s3.application.port.in;

import de.muenchen.refarch.integration.s3.domain.exception.FileSystemAccessException;
import de.muenchen.refarch.integration.s3.domain.model.FileSize;

public interface FileOperationsInPort {
    boolean fileExists(final String path) throws FileSystemAccessException;

    void deleteFile(final String pathToFile) throws FileSystemAccessException;

    FileSize getFileSize(final String pathToFile) throws FileSystemAccessException;
}
