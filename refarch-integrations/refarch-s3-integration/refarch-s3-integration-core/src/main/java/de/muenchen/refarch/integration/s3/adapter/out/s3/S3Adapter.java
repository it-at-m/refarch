package de.muenchen.refarch.integration.s3.adapter.out.s3;

import de.muenchen.refarch.integration.s3.application.port.out.S3OutPort;
import de.muenchen.refarch.integration.s3.domain.exception.FileSystemAccessException;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IteratorUtils;

@Slf4j
public class S3Adapter implements S3OutPort {

    /**
     * Response code from S3 storage when an object cannot be found.
     */
    private static final String RESPONSE_CODE_NO_SUCH_KEY = "NoSuchKey";

    private final String bucketName;
    private final MinioClient client;

    /**
     * Ctor.
     *
     * @param bucketName to which this Repository should connect.
     * @param client to communicate with the s3 storage.
     * @param s3InitialConnectionTest to enable initial connection test to the s3 storage when true.
     * @throws FileSystemAccessException if the initial connection test fails.
     */
    public S3Adapter(
            final String bucketName,
            final MinioClient client,
            final boolean s3InitialConnectionTest) throws FileSystemAccessException {
        this.bucketName = bucketName;
        this.client = client;
        if (s3InitialConnectionTest) {
            this.initialConnectionTest(bucketName, client);
        }
    }

    /**
     * Checks whether a file exists in the specified path.
     *
     * @param path the path of the file to be checked
     * @return true if the file exists, false otherwise
     * @throws FileSystemAccessException if there is an exception while accessing the file system
     */
    @Override
    public boolean fileExists(final String path) throws FileSystemAccessException {
        try {
            client.statObject(StatObjectArgs.builder()
                    .bucket(bucketName).object(path).build());
        } catch (final ErrorResponseException errorResponseException) {
            if (RESPONSE_CODE_NO_SUCH_KEY.equals(errorResponseException.errorResponse().code())) return false;
            else
                throw new FileSystemAccessException(errorResponseException.errorResponse().code(), errorResponseException);
        } catch (InsufficientDataException | InternalException | InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException
                | ServerException | XmlParserException exception) {
            final String message = String.format("Failed to request metadata for file %s.", path);
            log.error(message, exception);
            throw new FileSystemAccessException(message, exception);
        }
        return true;
    }

    /**
     * Returns the paths to the files in a given folder.
     *
     * @param folder The folder. The path must be absolute and without specifying the bucket. Example 1:
     *            Folder in bucket: "BUCKET/folder" Specification in
     *            parameter: "folder" Example 2: Folder in bucket: "BUCKET/folder/subfolder"
     *            Specification in parameter: "folder/subfolder"
     * @return the paths to the files in a given folder. Also returns the paths to the files in
     *         subfolders.
     * @throws FileSystemAccessException if the paths cannot be downloaded.
     */
    @Override
    public Set<String> getFilePathsFromFolder(final String folder) throws FileSystemAccessException {
        try {
            final ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder()
                    .bucket(this.bucketName)
                    .prefix(folder)
                    .recursive(true)
                    .build();
            final List<Result<Item>> resultItemList = IteratorUtils.toList(this.client.listObjects(listObjectsArgs).iterator());
            final Set<String> filepathesFromFolder = new HashSet<>();
            for (final Result<Item> resultItem : resultItemList) {
                filepathesFromFolder.add(resultItem.get().objectName());
            }
            return filepathesFromFolder;
        } catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException | IOException exception) {
            final String message = String.format("Failed to extract file paths from folder %s.", folder);
            log.error(message, exception);
            throw new FileSystemAccessException(message, exception);
        }
    }

    /**
     * Retrieves the sizes of all files within a specified folder.
     *
     * @param folder the folder path for which to retrieve file sizes.
     * @return a map where the keys are file paths and the values are the corresponding file sizes in
     *         bytes.
     * @throws FileSystemAccessException if the file sizes cannot be retrieved.
     */
    @Override
    public Map<String, Long> getFileSizesFromFolder(final String folder) throws FileSystemAccessException {
        try {
            final ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder()
                    .bucket(this.bucketName)
                    .prefix(folder)
                    .recursive(true)
                    .build();
            final List<Result<Item>> resultItemList = IteratorUtils.toList(this.client.listObjects(listObjectsArgs).iterator());
            final Map<String, Long> filePathsFromFolder = new HashMap<>();
            for (final Result<Item> resultItem : resultItemList) {
                filePathsFromFolder.put(resultItem.get().objectName(), resultItem.get().size());
            }
            return filePathsFromFolder;
        } catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException | IOException exception) {
            final String message = String.format("Failed to extract file paths from folder %s.", folder);
            log.error(message, exception);
            throw new FileSystemAccessException(message, exception);
        }
    }

    /**
     * Retrieves the size of a specified file.
     *
     * @param pathToFile the path of the file for which to retrieve the size.
     * @return the size of the file in bytes.
     * @throws FileSystemAccessException if the file size cannot be retrieved.
     */
    @Override
    public long getFileSize(final String pathToFile) throws FileSystemAccessException {
        try {
            return client.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(pathToFile)
                    .build())
                    .size();
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException | InvalidResponseException | IOException
                | NoSuchAlgorithmException | ServerException | XmlParserException exception) {
            final String message = String.format("Failed to request size of file %s.", pathToFile);
            log.error(message, exception);
            throw new FileSystemAccessException(message, exception);
        }
    }

    /**
     * Deletes the file given in the parameter.
     *
     * @param pathToFile The path to the file. The path must be absolute and without specifying the
     *            bucket. Example: File in bucket:
     *            "BUCKET/outerFolder/innerFolder/thefile.csv" Specification in parameter:
     *            "outerFolder/innerFolder/thefile.csv"
     * @throws FileSystemAccessException if the file cannot be deleted.
     */
    @Override
    public void deleteFile(final String pathToFile) throws FileSystemAccessException {
        try {
            final RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                    .bucket(this.bucketName)
                    .object(pathToFile)
                    .build();
            this.client.removeObject(removeObjectArgs);
        } catch (final InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
                | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
                | IllegalArgumentException | IOException exception) {
            final String message = String.format("Failed to delete file %s.", pathToFile);
            log.error(message, exception);
            throw new FileSystemAccessException(message, exception);
        }
    }

    /**
     * Creates the presigned URL fora file to the given file path.
     *
     * @param pathToFile The path to the file. The path must be absolute and without specifying the
     *            bucket.<br>
     *            Example:<br>
     *            File in bucket:<br>
     *            "BUCKET/outerFolder/innerFolder/thefile.csv"<br>
     *            Specification in parameter: "outerFolder/innerFolder/thefile.csv"<br>
     * @param action to determine the file permissions.
     * @param expiresInMinutes to define the validity period of the presigned URL.
     * @return the presigned URL for a file.
     * @throws FileSystemAccessException if the presigned URL cannot be created.
     */
    @Override
    public String getPresignedUrl(final String pathToFile, final Method action, final int expiresInMinutes) throws FileSystemAccessException {
        try {
            final GetPresignedObjectUrlArgs presignedUrlArgs = GetPresignedObjectUrlArgs.builder()
                    .method(action)
                    .bucket(this.bucketName)
                    .object(pathToFile)
                    .expiry(expiresInMinutes, TimeUnit.MINUTES)
                    .build();
            return this.client.getPresignedObjectUrl(presignedUrlArgs);
        } catch (final InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
                | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
                | IllegalArgumentException | IOException exception) {
            final String message = String.format("Failed to create presigned url for file %s. in mode %s", pathToFile, action);
            log.error(message, exception);
            throw new FileSystemAccessException(message, exception);
        }
    }

    /**
     * Performs an initial connection test against the S3 storage.
     *
     * @param bucketName to which this Repository should connect.
     * @param client to communicate with the s3 storage.
     * @throws FileSystemAccessException if the initial connection test fails.
     */
    private void initialConnectionTest(final String bucketName, final MinioClient client) throws FileSystemAccessException {
        try {
            final boolean bucketExists = client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                final String message = "S3 bucket does not exist.";
                log.error(message);
                throw new FileSystemAccessException(message);
            }
        } catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException | IOException exception) {
            final String message = "S3 initialization failed.";
            log.error(message, exception);
            throw new FileSystemAccessException(message, exception);
        }
    }

}
