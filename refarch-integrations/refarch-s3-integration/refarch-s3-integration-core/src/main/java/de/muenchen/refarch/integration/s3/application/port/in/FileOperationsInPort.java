package de.muenchen.refarch.integration.s3.application.port.in;

import de.muenchen.refarch.integration.s3.domain.exception.FileSystemAccessException;
import de.muenchen.refarch.integration.s3.domain.model.FileSize;

public interface FileOperationsInPort {
    boolean fileExists(String path) throws FileSystemAccessException;

    void deleteFile(String pathToFile) throws FileSystemAccessException;

    FileSize getFileSize(String pathToFile) throws FileSystemAccessException;
}
