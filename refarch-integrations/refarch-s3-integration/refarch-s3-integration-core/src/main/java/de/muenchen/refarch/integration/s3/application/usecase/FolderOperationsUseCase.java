package de.muenchen.refarch.integration.s3.application.usecase;

import de.muenchen.refarch.integration.s3.application.port.in.FolderOperationsInPort;
import de.muenchen.refarch.integration.s3.application.port.out.S3OutPort;
import de.muenchen.refarch.integration.s3.domain.exception.FileSystemAccessException;
import de.muenchen.refarch.integration.s3.domain.model.FileMetadata;
import de.muenchen.refarch.integration.s3.domain.model.FileSizesInFolder;
import de.muenchen.refarch.integration.s3.domain.model.FilesInFolder;
import de.muenchen.refarch.integration.s3.domain.model.FilesMetadataInFolder;
import de.muenchen.refarch.integration.s3.domain.validation.FolderInFilePathValidator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderOperationsUseCase implements FolderOperationsInPort {

    private final S3OutPort s3OutPort;

    /**
     * The method adds a path separator to the end of the parameter if no separator is already added.
     *
     * @param pathToFolder to add a separator.
     * @return the path to folder
     */
    public static String addPathSeparatorToTheEnd(final String pathToFolder) {
        String correctedPathToFolder = pathToFolder;
        if (StringUtils.isNotEmpty(pathToFolder) &&
                !StringUtils.endsWith(pathToFolder, FolderInFilePathValidator.SEPARATOR)) {
            correctedPathToFolder = correctedPathToFolder + FolderInFilePathValidator.SEPARATOR;
        }
        return correctedPathToFolder;
    }

    /**
     * Deletes the folder with all containing files specified in the parameter.
     *
     * @param pathToFolder identifies the path to the folder.
     * @throws FileSystemAccessException if the S3 storage cannot be accessed.
     */
    @Override
    public void deleteFolder(@NotNull final String pathToFolder) throws FileSystemAccessException {
        final String pathToFolderWithSeparatorAtTheEnd = addPathSeparatorToTheEnd(pathToFolder);
        final Set<String> filePathsInFolder = this.s3OutPort.getFilePathsFromFolder(pathToFolderWithSeparatorAtTheEnd);
        if (filePathsInFolder.isEmpty()) {
            log.info("Folder is empty in s3");
        } else {
            // Delete all files on S3
            log.info("Deleting {} files in folder {}", filePathsInFolder.size(), pathToFolderWithSeparatorAtTheEnd);
            for (final String pathToFile : filePathsInFolder) {
                // Delete file on S3
                this.s3OutPort.deleteFile(pathToFile);
            }
        }
    }

    /**
     * Returns all files identified by file paths for all files contained within the folder and
     * subfolder recursively.
     *
     * @param pathToFolder identifies the path to the folder.
     * @return the paths to the files within the folder and subfolder.
     * @throws FileSystemAccessException if the S3 storage cannot be accessed.
     */
    @NotNull
    @Override
    public FilesInFolder getAllFilesInFolderRecursively(@NotNull final String pathToFolder) throws FileSystemAccessException {
        final String pathToFolderWithSeparatorAtTheEnd = addPathSeparatorToTheEnd(pathToFolder);
        final Set<String> filePathsInFolder = this.s3OutPort.getFilePathsFromFolder(pathToFolderWithSeparatorAtTheEnd);
        return new FilesInFolder(filePathsInFolder);
    }

    /**
     * Returns the metadata of all files identified by file paths for all files contained within
     * the folder and subfolder recursively.
     *
     * @param pathToFolder identifies the path to the folder.
     * @return the metadata of the files within the folder and subfolder.
     * @throws FileSystemAccessException if the S3 storage cannot be accessed.
     */
    @NotNull
    @Override
    public FilesMetadataInFolder getMetadataOfAllFilesInFolderRecursively(@NotNull final String pathToFolder) throws FileSystemAccessException {
        final String pathToFolderWithSeparatorAtTheEnd = addPathSeparatorToTheEnd(pathToFolder);
        final List<FileMetadata> filePathsInFolder = this.s3OutPort.getMetadataOfFilesFromFolder(pathToFolderWithSeparatorAtTheEnd);
        return new FilesMetadataInFolder(filePathsInFolder);
    }

    /**
     * Retrieves the sizes of all files within the specified folder and its subfolders recursively.
     *
     * @param pathToFolder the path to the folder whose file sizes are to be retrieved.
     * @return a {@link FileSizesInFolder} object containing the sizes of all files within the folder
     *         and its subfolders.
     * @throws FileSystemAccessException if the S3 storage cannot be accessed.
     */
    @Override
    public FileSizesInFolder getAllFileSizesInFolderRecursively(@NotNull final String pathToFolder) throws FileSystemAccessException {
        final String pathToFolderWithSeparatorAtTheEnd = addPathSeparatorToTheEnd(pathToFolder);
        final Map<String, Long> mapFilePathsToSize = this.s3OutPort.getFileSizesFromFolder(pathToFolderWithSeparatorAtTheEnd);
        return new FileSizesInFolder(mapFilePathsToSize);
    }

}
