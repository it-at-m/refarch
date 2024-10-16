package de.muenchen.refarch.integration.s3.application.port.out;

import de.muenchen.refarch.integration.s3.domain.exception.FileSystemAccessException;
import io.minio.http.Method;
import java.util.Map;
import java.util.Set;

public interface S3OutPort {
    boolean fileExists(String path) throws FileSystemAccessException;

    Set<String> getFilePathsFromFolder(String folder) throws FileSystemAccessException;

    void deleteFile(String pathToFile) throws FileSystemAccessException;

    String getPresignedUrl(String pathToFile, Method action, int expiresInMinutes) throws FileSystemAccessException;

    Map<String, Long> getFileSizesFromFolder(String folder) throws FileSystemAccessException;

    long getFileSize(String pathToFile) throws FileSystemAccessException;
}
