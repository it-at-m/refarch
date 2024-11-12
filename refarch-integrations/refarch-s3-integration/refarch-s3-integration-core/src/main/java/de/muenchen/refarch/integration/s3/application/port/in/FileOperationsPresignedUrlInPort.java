package de.muenchen.refarch.integration.s3.application.port.in;

import de.muenchen.refarch.integration.s3.domain.exception.FileSystemAccessException;
import de.muenchen.refarch.integration.s3.domain.model.FileData;
import de.muenchen.refarch.integration.s3.domain.model.PresignedUrl;
import io.minio.http.Method;
import java.util.List;

/**
 * Port describing the main operations on files.
 */
public interface FileOperationsPresignedUrlInPort {
    List<PresignedUrl> getPresignedUrls(List<String> paths, Method action, int expiresInMinutes) throws FileSystemAccessException;

    PresignedUrl getPresignedUrl(String path, Method action, int expiresInMinutes) throws FileSystemAccessException;

    PresignedUrl getFile(String pathToFile, int expiresInMinutes) throws FileSystemAccessException;

    PresignedUrl saveFile(FileData fileData) throws FileSystemAccessException;

    PresignedUrl updateFile(FileData fileData) throws FileSystemAccessException;

    PresignedUrl deleteFile(String pathToFile, int expiresInMinutes) throws FileSystemAccessException;
}
