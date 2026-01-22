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
     *
     * @param bucket the bucket name, must not be null
     * @param pathToFolder the path interpreted as a key prefix; a trailing slash is optional, must not
     *            be null
     * @param recursive if {@code true}, lists all objects recursively beneath the prefix; if
     *            {@code false},
     *            lists only the immediate children
     * @return a possibly empty list of file metadata; never {@code null}
     * @throws S3Exception if listing fails due to an underlying storage error
     */
    List<FileMetadata> getFilesInFolder(@NotNull String bucket, @NotNull String pathToFolder, boolean recursive) throws S3Exception;
}
