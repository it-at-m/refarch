package de.muenchen.refarch.integration.s3.application.port.out;

import de.muenchen.refarch.integration.s3.domain.exception.S3Exception;
import de.muenchen.refarch.integration.s3.domain.model.FileMetadata;
import de.muenchen.refarch.integration.s3.domain.model.FileReference;
import de.muenchen.refarch.integration.s3.domain.model.PresignedUrl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.File;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import org.springframework.validation.annotation.Validated;

/**
 * Outbound port defining operations to interact with an S3-compatible object storage.
 * Implementations are responsible for translating these domain-level operations into SDK calls
 * and for converting SDK-specific exceptions into {@link S3Exception}.
 */
@Validated
public interface S3OutPort {

    /**
     * Checks if an object exists at the given file reference.
     *
     * @param fileReference the bucket and path identifying the object (must not be null)
     * @return true if the object exists; false if the object does not exist
     * @throws S3Exception if a client or network error occurs while checking existence
     */
    boolean fileExists(@NotNull @Valid FileReference fileReference) throws S3Exception;

    /**
     * Stores content at the given file reference.
     * The provided {@code content} stream will be read entirely by the implementation.
     * The caller is responsible for closing the stream after this method returns.
     *
     * @param fileReference the bucket and path where the content will be stored (must not be null)
     * @param content the input stream containing the data to upload (must not be null)
     * @param contentLength the total number of bytes in {@code content}; used by some SDKs to optimize
     *            upload
     * @throws S3Exception if the upload fails due to client, network, or service issues
     */
    void saveFile(@NotNull @Valid FileReference fileReference, @NotNull InputStream content, long contentLength) throws S3Exception;

    /**
     * Convenience overload to upload content from a local file.
     *
     * @param fileReference the target file reference (bucket/key) (must not be null)
     * @param file the local file to upload (must not be null)
     * @throws S3Exception if the upload fails due to client, network, or service issues
     */
    void saveFile(@NotNull @Valid FileReference fileReference, @NotNull File file) throws S3Exception;

    /**
     * Retrieves object metadata for the given file reference without downloading the object body.
     *
     * @param fileReference the bucket and path identifying the object (must not be null)
     * @return metadata describing the object (size, etag, last-modified, etc.)
     * @throws S3Exception if the metadata retrieval fails due to client, network, or service issues
     */
    FileMetadata getFileMetadata(@NotNull @Valid FileReference fileReference) throws S3Exception;

    /**
     * Creates a presigned URL allowing the specified action on the object for a limited lifetime.
     *
     * Supported actions typically include {@link PresignedUrl.Action#GET},
     * {@link PresignedUrl.Action#PUT},
     * {@link PresignedUrl.Action#DELETE}, and {@link PresignedUrl.Action#HEAD}.
     * The effective maximum lifetime may be constrained by the underlying provider (e.g., AWS S3 up to
     * 7 days).
     *
     * @param fileReference the bucket and path identifying the object (must not be null)
     * @param action the operation to authorize via the presigned URL (must not be null)
     * @param lifetime the duration for which the URL remains valid (must not be null)
     * @return a {@link PresignedUrl} containing the URL, target path, and action
     * @throws S3Exception if URL creation fails due to client, network, or service issues
     */
    PresignedUrl getPresignedUrl(@NotNull @Valid FileReference fileReference, @NotNull PresignedUrl.Action action, @NotNull Duration lifetime)
            throws S3Exception;

    /**
     * Downloads the object content as an input stream.
     * The caller is responsible for consuming and closing the returned stream.
     *
     * @param fileReference the bucket and path identifying the object (must not be null)
     * @return an input stream to read the object's content
     * @throws S3Exception if the download fails due to client, network, or service issues
     */
    InputStream getFileContent(@NotNull @Valid FileReference fileReference) throws S3Exception;

    /**
     * Deletes the object at the given file reference.
     *
     * @param fileReference the bucket and path identifying the object to delete (must not be null)
     * @throws S3Exception if the delete operation fails due to client, network, or service issues
     */
    void deleteFile(@NotNull @Valid FileReference fileReference) throws S3Exception;

    /**
     * Lists files in the specified bucket starting with the given prefix with pagination controls.
     *
     * @param bucket the bucket name (must not be blank)
     * @param prefix the prefix under which to list objects (must not be blank)
     * @param maxKeys maximum number of keys to return in this page (provider limits may apply, e.g.,
     *            1–1000)
     * @param marker key to start after when listing objects (used to continue from a previous truncated
     *            response);
     *            pass null or empty to start from the beginning
     * @return a list of metadata entries for the objects found under the prefix (up to {@code maxKeys}
     *         items)
     * @throws S3Exception if listing fails due to client, network, or service issues
     */
    List<FileMetadata> getFilesWithPrefix(@NotBlank String bucket, @NotBlank String prefix, int maxKeys, String marker) throws S3Exception;
}
