package de.muenchen.refarch.integration.s3.application.port.in;

import de.muenchen.refarch.integration.s3.domain.exception.S3Exception;
import de.muenchen.refarch.integration.s3.domain.model.FileMetadata;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Inbound application port for listing and browsing objects within a logical folder (prefix)
 * in an object store (e.g. S3).
 */
public interface FolderOperationsInPort {

    /**
     * Lists files under the given folder (prefix) within the specified bucket.
     * Uses default pagination (maxKeys = 1000) and no marker.
     *
     * @param bucket the bucket name, must not be null
     * @param pathToFolder the path interpreted as a key prefix; a trailing slash is optional, must not
     *            be null
     * @param recursive if {@code true}, lists all objects recursively beneath the prefix; if
     *            {@code false},
     *            lists only the immediate children
     * @return a possibly empty list of file metadata
     * @throws S3Exception if listing fails due to an underlying storage error
     */
    List<FileMetadata> getFilesInFolder(@NotNull String bucket, @NotNull String pathToFolder, boolean recursive) throws S3Exception;

    /**
     * Lists files under the given folder (prefix) within the specified bucket with pagination controls.
     *
     * @param bucket the bucket name, must not be null
     * @param pathToFolder the path interpreted as a key prefix; a trailing slash is optional, must not
     *            be null
     * @param recursive if {@code true}, lists all objects recursively beneath the prefix; if
     *            {@code false}, lists only the immediate children
     * @param maxKeys maximum number of keys to return in this page (provider limits may apply, e.g.,
     *            1–1000)
     * @param marker key to start after when listing objects (used to continue from a previous truncated
     *            response); pass null or empty to start from the beginning
     * @return a possibly empty list of file metadata
     * @throws S3Exception if listing fails due to an underlying storage error
     */
    List<FileMetadata> getFilesInFolder(@NotNull String bucket, @NotNull String pathToFolder, boolean recursive, int maxKeys, String marker)
            throws S3Exception;
}
