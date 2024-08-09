package de.muenchen.refarch.integration.s3.application.usecase;

import de.muenchen.refarch.integration.s3.adapter.in.rest.validation.FolderInFilePathValidator;
import de.muenchen.refarch.integration.s3.application.port.in.FileOperationsPresignedUrlInPort;
import de.muenchen.refarch.integration.s3.application.port.out.S3OutPort;
import de.muenchen.refarch.integration.s3.domain.exception.FileExistenceException;
import de.muenchen.refarch.integration.s3.domain.exception.FileSystemAccessException;
import de.muenchen.refarch.integration.s3.domain.model.FileData;
import de.muenchen.refarch.integration.s3.domain.model.PresignedUrl;
import io.minio.http.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileOperationsPresignedUrlUseCase implements FileOperationsPresignedUrlInPort {

    private final S3OutPort s3OutPort;

    /**
     * Return the path to the folder for the given file path in the parameter.
     * <p>
     * pathToFile: FOLDER/SUBFOLDER/file.txt pathToFolder: FOLDER/SUBFOLDER
     *
     * @param pathToFile for which the path to folder should be returned.
     * @return the path to the folder for the given path to file.
     */
    public static String getPathToFolder(final String pathToFile) {
        return StringUtils.contains(pathToFile, FolderInFilePathValidator.SEPARATOR)
                ? StringUtils.substringBeforeLast(pathToFile, FolderInFilePathValidator.SEPARATOR)
                : StringUtils.EMPTY;
    }

    /**
     * Get a list of presigned urls for all files in the paths. If the path is a file the presigned url
     * for the file is returned.
     *
     * @param paths list of paths to files and/or folders
     * @param action http method for the presigned url
     * @param expiresInMinutes presigned url expiration time
     * @return list of pre-signed urls.
     * @throws FileSystemAccessException on S3 access errors.
     * @throws FileExistenceException if file doesn't exist.
     */
    @Override
    public List<PresignedUrl> getPresignedUrls(final List<String> paths, final Method action, final int expiresInMinutes)
            throws FileSystemAccessException, FileExistenceException {
        final List<PresignedUrl> presignedUrls = new ArrayList<>();
        for (String p : paths) {
            presignedUrls.addAll(this.getPresignedUrls(p, action, expiresInMinutes));
        }
        return presignedUrls;
    }

    /**
     * Get a single presigned url for the path.
     *
     * @param path path to file or folder
     * @param action http method for the presigned url
     * @param expiresInMinutes presigned url expiration time
     * @return pre-signed url.
     * @throws FileSystemAccessException on S3 access errors.
     */
    @Override
    public PresignedUrl getPresignedUrl(final String path, final Method action, final int expiresInMinutes) throws FileSystemAccessException {
        return new PresignedUrl(this.s3OutPort.getPresignedUrl(path, action, expiresInMinutes), path, action.toString());
    }

    /**
     * Creates a presigned URL to fetch the file specified in the parameter from the S3 storage.
     *
     * @param pathToFile identifies the path to file.
     * @param expiresInMinutes to define the validity period of the presigned URL.
     * @throws FileExistenceException if the file does not exist in the folder.
     * @throws FileSystemAccessException if the S3 storage cannot be accessed.
     */
    @Override
    public PresignedUrl getFile(final String pathToFile, final int expiresInMinutes) throws FileExistenceException, FileSystemAccessException {
        if (!this.fileExists(pathToFile)) {
            final String message = String.format("The file %s does not exists.", pathToFile);
            log.error(message);
            throw new FileExistenceException(message);
        }
        return this.getPresignedUrl(pathToFile, Method.GET, expiresInMinutes);
    }

    /**
     * Creates a presigned URL to store the file specified in the parameter within the S3 storage. The
     * file must not exist yet.
     *
     * @param fileData with the file metadata to save.
     * @throws FileExistenceException if the file already exists.
     * @throws FileSystemAccessException if the S3 storage cannot be accessed.
     */

    @Override
    public PresignedUrl saveFile(final FileData fileData) throws FileSystemAccessException, FileExistenceException {
        if (this.fileExists(fileData.pathToFile())) {
            final String message = String.format("The file %s does exists.", fileData.pathToFile());
            log.error(message);
            throw new FileExistenceException(message);
        }
        return this.getPresignedUrl(fileData.pathToFile(), Method.PUT, fileData.expiresInMinutes());
    }

    /**
     * Creates a presigned URL to overwrite the file specified in the parameter within the S3 storage.
     * <p>
     * If the file does not yet exist in the S3 storage, it is newly created.
     *
     * @param fileData with the file metadata for re-saving.
     * @throws FileSystemAccessException if the S3 storage cannot be accessed.
     */
    @Override
    public PresignedUrl updateFile(final FileData fileData) throws FileSystemAccessException {
        return this.getPresignedUrl(fileData.pathToFile(), Method.PUT, fileData.expiresInMinutes());
    }

    /**
     * Creates a presigned URL to delete the file specified in the parameter from the S3 storage.
     *
     * @param pathToFile identifies the path to file.
     * @param expiresInMinutes to define the validity period of the presigned URL.
     * @throws FileExistenceException if the file does not exist in the folder.
     * @throws FileSystemAccessException if the S3 storage cannot be accessed.
     */
    @Override
    public PresignedUrl deleteFile(final String pathToFile, final int expiresInMinutes) throws FileExistenceException, FileSystemAccessException {
        if (!this.fileExists(pathToFile)) {
            final String message = String.format("The file %s does not exists.", pathToFile);
            log.error(message);
            throw new FileExistenceException(message);
        }
        return this.getPresignedUrl(pathToFile, Method.DELETE, expiresInMinutes);
    }

    private boolean fileExists(final String filePath) throws FileSystemAccessException {
        return this.s3OutPort.fileExists(filePath);
    }

    /**
     * Get a list of presigned urls for all files in the path. If the path is a file the presigned url
     * for the file is returned.
     *
     * @param path path to file or folder
     * @param action http method for the presigned url
     * @param expiresInMinutes presigned url expiration time
     * @return list of pre-signed urls.
     * @throws FileSystemAccessException on S3 access errors.
     * @throws FileExistenceException if file doesn't exist.
     */
    private List<PresignedUrl> getPresignedUrls(final String path, final Method action, final int expiresInMinutes)
            throws FileSystemAccessException, FileExistenceException {
        // special case file creation (POST)
        // Use method PUT and return a single presignedUrl for the file the user wants to create
        if (action.equals(Method.POST)) {
            return List.of(this.getPresignedUrl(path, Method.PUT, expiresInMinutes));
        }

        // PUT, GET, DELETE return single presignedUrl if path is file. Return list of presignedUrls if path is directory
        final List<String> paths = new ArrayList<>(this.s3OutPort.getFilePathsFromFolder(path));
        final List<PresignedUrl> presignedUrlList = paths.stream()
                .map(filePath -> this.getPresignedUrlForFile(filePath, action, expiresInMinutes))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (presignedUrlList.isEmpty()) {
            final String message = String.format("The file %s does not exist.", path);
            log.error(message);
            throw new FileExistenceException(message);
        }

        return presignedUrlList;
    }

    private PresignedUrl getPresignedUrlForFile(final String filePath, final Method action, final int expiresInMinutes) {
        try {
            final String presignedUrl = this.s3OutPort.getPresignedUrl(filePath, action, expiresInMinutes);
            return new PresignedUrl(presignedUrl, filePath, action.toString());
        } catch (final FileSystemAccessException e) {
            log.warn("File not found on path {}", filePath);
        }
        return null;
    }

}
