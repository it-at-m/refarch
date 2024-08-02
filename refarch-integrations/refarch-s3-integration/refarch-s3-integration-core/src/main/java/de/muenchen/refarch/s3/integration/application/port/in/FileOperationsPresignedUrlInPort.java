package de.muenchen.refarch.s3.integration.application.port.in;

import de.muenchen.refarch.s3.integration.domain.exception.FileExistenceException;
import de.muenchen.refarch.s3.integration.domain.exception.FileSystemAccessException;
import de.muenchen.refarch.s3.integration.domain.model.FileData;
import de.muenchen.refarch.s3.integration.domain.model.PresignedUrl;
import io.minio.http.Method;
import java.util.List;

/**
 * Port describing the main operations on files.
 */
public interface FileOperationsPresignedUrlInPort {
    List<PresignedUrl> getPresignedUrls(List<String> paths, Method action, int expiresInMinutes) throws FileSystemAccessException, FileExistenceException;

    PresignedUrl getPresignedUrl(String path, Method action, int expiresInMinutes) throws FileSystemAccessException;

    PresignedUrl getFile(final String pathToFile, final int expiresInMinutes) throws FileExistenceException, FileSystemAccessException;

    PresignedUrl saveFile(final FileData fileData) throws FileSystemAccessException, FileExistenceException;

    PresignedUrl updateFile(final FileData fileData) throws FileSystemAccessException;

    PresignedUrl deleteFile(final String pathToFile, final int expiresInMinutes) throws FileExistenceException, FileSystemAccessException;
}
