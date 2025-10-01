package de.muenchen.refarch.integration.s3.application.port.in;

import de.muenchen.refarch.integration.s3.domain.exception.FileSystemAccessException;
import de.muenchen.refarch.integration.s3.domain.model.FileSizesInFolder;
import de.muenchen.refarch.integration.s3.domain.model.FilesInFolder;
import de.muenchen.refarch.integration.s3.domain.model.FilesMetadataInFolder;
import jakarta.validation.constraints.NotNull;

/**
 * Describes operations on a folder.
 */
public interface FolderOperationsInPort {

    /**
     * Retrieves a list of files in a folder.
     *
     * @param pathToFolder path to folder.
     * @return list of files in folder.
     * @throws FileSystemAccessException on access errors.
     */
    @NotNull FilesInFolder getAllFilesInFolderRecursively(@NotNull String pathToFolder) throws FileSystemAccessException;

    /**
     * Retrieves a list of metadata for files in a folder.
     *
     * @param pathToFolder path to folder.
     * @return list of metadata for files in folder.
     * @throws FileSystemAccessException on access errors.
     */
    @NotNull FilesMetadataInFolder getMetadataOfAllFilesInFolderRecursively(@NotNull String pathToFolder) throws FileSystemAccessException;

    /**
     * Retrieves the sizes of all files within a specified folder.
     *
     * @param pathToFolder the folder path for which to retrieve file sizes.
     * @return wraps a map where the keys are file paths and the values are the corresponding file sizes
     *         in bytes.
     * @throws FileSystemAccessException on access errors.
     */
    FileSizesInFolder getAllFileSizesInFolderRecursively(@NotNull String pathToFolder) throws FileSystemAccessException;

    void deleteFolder(@NotNull String pathToFolder) throws FileSystemAccessException;
}
