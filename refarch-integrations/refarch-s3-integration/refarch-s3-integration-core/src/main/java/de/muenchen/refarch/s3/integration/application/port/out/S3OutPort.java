package de.muenchen.refarch.s3.integration.application.port.out;

import de.muenchen.refarch.s3.integration.domain.exception.FileSystemAccessException;
import io.minio.http.Method;
import java.util.Map;
import java.util.Set;

public interface S3OutPort {
    boolean fileExists(final String path) throws FileSystemAccessException;

    Set<String> getFilePathsFromFolder(final String folder) throws FileSystemAccessException;

    void deleteFile(final String pathToFile) throws FileSystemAccessException;

    String getPresignedUrl(final String pathToFile, final Method action, final int expiresInMinutes) throws FileSystemAccessException;

    Map<String, Long> getFileSizesFromFolder(final String folder) throws FileSystemAccessException;

    long getFileSize(final String pathToFile) throws FileSystemAccessException;
}
