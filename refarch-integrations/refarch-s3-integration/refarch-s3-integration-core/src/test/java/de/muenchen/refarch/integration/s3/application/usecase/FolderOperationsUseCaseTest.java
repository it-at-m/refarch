package de.muenchen.refarch.integration.s3.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import de.muenchen.refarch.integration.s3.adapter.out.s3.S3Adapter;
import de.muenchen.refarch.integration.s3.domain.exception.FileSystemAccessException;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import de.muenchen.refarch.integration.s3.domain.model.FileMetadata;
import de.muenchen.refarch.integration.s3.domain.model.FilesMetadataInFolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FolderOperationsUseCaseTest {

    @Mock
    private S3Adapter s3Adapter;

    private FolderOperationsUseCase folderHandlingService;

    @BeforeEach
    public void beforeEach() {
        this.folderHandlingService = new FolderOperationsUseCase(this.s3Adapter);
        Mockito.reset(this.s3Adapter);
    }

    @Test
    void deleteFolder() throws FileSystemAccessException {
        final String pathToFile = "folder/file.txt";
        final String pathToFolder = "folder";
        final String pathToFolderWithSeparator = pathToFolder + "/";

        Mockito.when(this.s3Adapter.getFilePathsFromFolder(pathToFolderWithSeparator)).thenReturn(new HashSet<>(List.of(pathToFile)));
        assertDoesNotThrow(() -> this.folderHandlingService.deleteFolder(pathToFolder));
        Mockito.verify(this.s3Adapter, Mockito.times(1)).deleteFile(pathToFile);
    }

    @Test
    void getMetadataOfAllFilesInFolderRecursively() throws FileSystemAccessException {
        final String pathToFolder = "folder";
        final String pathToFolderWithSeparator = pathToFolder + "/";

        Mockito.when(this.s3Adapter.getMetadataOfFilesFromFolder(pathToFolderWithSeparator)).thenReturn(List.of(new FileMetadata()));
        final AtomicReference<FilesMetadataInFolder> result = new AtomicReference<>();
        assertDoesNotThrow(() -> result.set(this.folderHandlingService.getMetadataOfAllFilesInFolderRecursively(pathToFolder)));
        final FilesMetadataInFolder expected = new FilesMetadataInFolder(List.of(new FileMetadata()));
        assertThat(result.get()).isEqualTo(expected);
        Mockito.verify(this.s3Adapter, Mockito.times(1)).getMetadataOfFilesFromFolder(pathToFolderWithSeparator);
    }

    @Test
    void addPathSeparatorToTheEnd() {
        assertThat(FolderOperationsUseCase.addPathSeparatorToTheEnd("folder/subfolder")).isEqualTo("folder/subfolder/");
        assertThat(FolderOperationsUseCase.addPathSeparatorToTheEnd("folder/subfolder/")).isEqualTo("folder/subfolder/");
        assertThat(FolderOperationsUseCase.addPathSeparatorToTheEnd("folder")).isEqualTo("folder/");
        assertThat(FolderOperationsUseCase.addPathSeparatorToTheEnd("folder/")).isEqualTo("folder/");
        assertThat(FolderOperationsUseCase.addPathSeparatorToTheEnd("folder//")).isEqualTo("folder//");
        assertThat(FolderOperationsUseCase.addPathSeparatorToTheEnd("")).isEqualTo("");
        assertThat(FolderOperationsUseCase.addPathSeparatorToTheEnd(null)).isNull();
    }

}
