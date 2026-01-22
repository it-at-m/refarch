package de.muenchen.refarch.integration.s3.application.port.in;

import de.muenchen.refarch.integration.s3.domain.exception.S3Exception;
import de.muenchen.refarch.integration.s3.domain.model.FileMetadata;
import de.muenchen.refarch.integration.s3.domain.model.FileReference;
import de.muenchen.refarch.integration.s3.domain.model.PresignedUrl;
import jakarta.validation.constraints.NotNull;
import java.io.File;
import java.io.InputStream;
import org.springframework.validation.annotation.Validated;

/**
 * Inbound application port exposing file-oriented operations backed by an object store
 * (for example, Amazon S3 or an S3-compatible service).
 */
@Validated
public interface FileOperationsInPort {

    /**
     * Checks whether an object exists for the given reference.
     *
     * @param fileReference the target file reference (bucket/key), must not be null
     * @return {@code true} if the object exists, {@code false} otherwise
     * @throws S3Exception if the existence check fails due to an underlying storage error
     */
    boolean fileExists(@NotNull FileReference fileReference) throws S3Exception;

    /**
     * Saves (creates or overwrites) the content of the referenced object using a known content length.
     * Implementations will consume the provided stream from its current position.
     *
     * @param fileReference the target file reference (bucket/key), must not be null
     * @param content the content to upload, must not be null
     * @param contentLength the exact number of bytes to upload
     * @throws S3Exception if the upload fails due to an underlying storage error
     */
    void saveFile(@NotNull FileReference fileReference, @NotNull InputStream content, long contentLength) throws S3Exception;

    /**
     * Convenience overload for saving content when the content length is unknown.
     * Implementations may buffer/stream in order to determine the content length or
     * use transfer strategies that do not require the length up front. Consider
     * using the length-aware method when the size is known to avoid additional I/O.
     *
     * @param fileReference the target file reference (bucket/key), must not be null
     * @param content the content to upload, must not be null
     * @throws S3Exception if the upload fails due to an underlying storage error
     */
    void saveFile(@NotNull FileReference fileReference, @NotNull InputStream content) throws S3Exception;

    /**
     * Convenience overload for saving content directly from a {@link java.io.File}.
     * Implementations should stream the file content and use its length for upload.
     *
     * @param fileReference the target file reference (bucket/key), must not be null
     * @param file the source file to upload, must not be null
     * @throws S3Exception if the upload fails due to an underlying storage error
     */
    void saveFile(@NotNull FileReference fileReference, @NotNull File file) throws S3Exception;

    /**
     * Retrieves metadata for the referenced object.
     *
     * @param fileReference the target file reference (bucket/key), must not be null
     * @return the file metadata
     * @throws S3Exception if retrieving metadata fails due to an underlying storage error
     */
    FileMetadata getFileMetadata(@NotNull FileReference fileReference) throws S3Exception;

    /**
     * Creates a presigned URL for the referenced object and action.
     *
     * @param fileReference the target file reference (bucket/key), must not be null
     * @param action the intended action (e.g., download or upload), must not be null
     * @param lifetime the validity period for the presigned URL; actual expiration may be constrained
     *            by the provider
     * @return a presigned URL encapsulating the action and expiration
     * @throws S3Exception if URL generation fails due to an underlying storage error
     */
    PresignedUrl getPresignedUrl(@NotNull FileReference fileReference, @NotNull PresignedUrl.Action action, @NotNull java.time.Duration lifetime)
            throws S3Exception;

    /**
     * Retrieves the content of the referenced object as a stream.
     * The caller is responsible for consuming and closing the returned stream.
     *
     * @param fileReference the target file reference (bucket/key), must not be null
     * @return an input stream for the object content
     * @throws S3Exception if fetching content fails due to an underlying storage error
     */
    InputStream getFileContent(@NotNull FileReference fileReference) throws S3Exception;

    /**
     * Deletes the referenced object.
     *
     * @param fileReference the target file reference (bucket/key), must not be null
     * @throws S3Exception if deletion fails due to an underlying storage error
     */
    void deleteFile(@NotNull FileReference fileReference) throws S3Exception;
}
