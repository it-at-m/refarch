package de.muenchen.refarch.integration.s3.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.refarch.integration.s3.adapter.out.s3.S3Adapter;
import de.muenchen.refarch.integration.s3.application.port.in.FileOperationsPresignedUrlInPort;
import de.muenchen.refarch.integration.s3.domain.exception.FileExistenceException;
import de.muenchen.refarch.integration.s3.domain.exception.FileSystemAccessException;
import de.muenchen.refarch.integration.s3.domain.model.FileData;
import de.muenchen.refarch.integration.s3.domain.model.PresignedUrl;
import io.minio.http.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
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
class FileOperationsPresignedUrlUseCaseTest {

    @Mock
    private S3Adapter s3Adapter;

    private FileOperationsPresignedUrlInPort fileOperations;

    @BeforeEach
    public void beforeEach() {
        this.fileOperations = new FileOperationsPresignedUrlUseCase(this.s3Adapter);
        Mockito.reset(this.s3Adapter);
    }

    @Test
    void getPresignedUrl() {
        final String pathToFile = "folder/test.txt";
        final int expiresInMinutes = 5;
        final List<Method> actions = List.of(Method.GET, Method.POST, Method.PUT, Method.DELETE);

        final String examplePresignedUrl = "some-presigned-url";

        actions.forEach(action -> {
            try {
                Mockito.when(this.s3Adapter.getPresignedUrl(pathToFile, action, expiresInMinutes)).thenReturn(examplePresignedUrl);

                final PresignedUrl presignedUrl = fileOperations.getPresignedUrl(pathToFile, action, expiresInMinutes);

                Assertions.assertEquals(examplePresignedUrl, presignedUrl.url());
                Assertions.assertEquals(action.toString(), presignedUrl.action());
                Assertions.assertEquals(pathToFile, presignedUrl.path());
            } catch (final FileSystemAccessException e) {
                Assertions.fail(e.getMessage());
            }
        });

    }

    @Test
    void getPresignedUrlForFile() throws FileSystemAccessException, FileExistenceException {
        final String pathToFile = "folder/test.txt";
        final int expiresInMinutes = 5;
        final List<Method> actions = List.of(Method.GET, Method.PUT, Method.DELETE);

        final String examplePresignedUrl = "some-presigned-url";

        // GET, PUT, DELETE
        actions.forEach(action -> {
            try {
                Mockito.when(this.s3Adapter.fileExists(pathToFile)).thenReturn(true);
                Mockito.when(this.s3Adapter.getFilePathsFromFolder(pathToFile)).thenReturn(Set.of(pathToFile));
                Mockito.when(this.s3Adapter.getPresignedUrl(pathToFile, action, expiresInMinutes)).thenReturn(examplePresignedUrl);

                final List<PresignedUrl> presignedUrls = this.fileOperations.getPresignedUrls(List.of(pathToFile), action, expiresInMinutes);

                Assertions.assertEquals(1, presignedUrls.size());
                presignedUrls.forEach(presignedUrl -> {
                    Assertions.assertEquals(presignedUrl.url(), examplePresignedUrl);
                    Assertions.assertEquals(presignedUrl.action(), action.toString());
                    Assertions.assertEquals(presignedUrl.path(), pathToFile);
                });
                Mockito.reset();
            } catch (final FileExistenceException | FileSystemAccessException e) {
                Assertions.fail(e.getMessage());
            }
        });

        // POST
        // special case POST is converted to PUT
        Mockito.when(this.s3Adapter.getPresignedUrl(pathToFile, Method.PUT, expiresInMinutes)).thenReturn(examplePresignedUrl);

        final List<PresignedUrl> presignedUrls = this.fileOperations.getPresignedUrls(List.of(pathToFile), Method.POST, expiresInMinutes);

        Assertions.assertEquals(1, presignedUrls.size());
        presignedUrls.forEach(presignedUrl -> {
            Assertions.assertEquals(presignedUrl.url(), examplePresignedUrl);
            Assertions.assertEquals(presignedUrl.action(), Method.PUT.toString());
            Assertions.assertEquals(presignedUrl.path(), pathToFile);
        });
    }

    @Test
    void getPresignedUrlsForDirectory() throws FileSystemAccessException, FileExistenceException {
        final String pathToDirectory = "folder/";
        final Set<String> files = Set.of("folder/test.txt", "folder/test1.txt");
        final int expiresInMinutes = 5;
        final List<Method> actions = List.of(Method.GET, Method.PUT, Method.DELETE);

        final String examplePresignedUrl = "some-presigned-url";

        // GET, PUT, DELETE
        actions.forEach(action -> {
            try {
                Mockito.when(this.s3Adapter.getFilePathsFromFolder(pathToDirectory)).thenReturn(files);
                for (final String file : files) {
                    Mockito.when(this.s3Adapter.getPresignedUrl(file, action, expiresInMinutes)).thenReturn(examplePresignedUrl);
                }

                final List<PresignedUrl> presignedUrls = this.fileOperations.getPresignedUrls(List.of(pathToDirectory), action, expiresInMinutes);

                Assertions.assertEquals(2, presignedUrls.size());
                presignedUrls.forEach(presignedUrl -> {
                    Assertions.assertEquals(presignedUrl.url(), examplePresignedUrl);
                    Assertions.assertEquals(presignedUrl.action(), action.toString());
                    Assertions.assertTrue(files.stream().anyMatch(file -> file.equals(presignedUrl.path())));
                });
                Mockito.reset();
            } catch (final FileExistenceException | FileSystemAccessException e) {
                Assertions.fail(e.getMessage());
            }
        });

        // POST
        // special case POST is converted to PUT
        Mockito.when(this.s3Adapter.getPresignedUrl(pathToDirectory, Method.PUT, expiresInMinutes)).thenReturn(examplePresignedUrl);

        final List<PresignedUrl> presignedUrls = this.fileOperations.getPresignedUrls(List.of(pathToDirectory), Method.POST, expiresInMinutes);

        Assertions.assertEquals(1, presignedUrls.size());
        presignedUrls.forEach(presignedUrl -> {
            Assertions.assertEquals(presignedUrl.url(), examplePresignedUrl);
            Assertions.assertEquals(presignedUrl.action(), Method.PUT.toString());
            Assertions.assertEquals(presignedUrl.path(), pathToDirectory);
        });
    }

    @Test
    void getPresignedUrlsForMultipleFiles() throws FileSystemAccessException, FileExistenceException {
        final List<String> pathToFiles = List.of("folder/first.txt", "folder/second.txt", "folder/third.txt");
        final int expiresInMinutes = 5;
        final List<Method> actions = List.of(Method.GET, Method.PUT, Method.DELETE);

        final String examplePresignedUrl = "some-presigned-url";

        // GET, PUT, DELETE
        actions.forEach(action -> {
            try {
                for (String file : pathToFiles) {
                    Mockito.when(this.s3Adapter.getFilePathsFromFolder(file)).thenReturn(Set.of(file));
                    Mockito.when(this.s3Adapter.getPresignedUrl(file, action, expiresInMinutes)).thenReturn(examplePresignedUrl);
                }

                final List<PresignedUrl> presignedUrls = this.fileOperations.getPresignedUrls(pathToFiles, action, expiresInMinutes);

                Assertions.assertEquals(3, presignedUrls.size());
                presignedUrls.forEach(presignedUrl -> {
                    Assertions.assertEquals(presignedUrl.url(), examplePresignedUrl);
                    Assertions.assertEquals(presignedUrl.action(), action.toString());
                });
                Mockito.reset();
            } catch (final FileExistenceException | FileSystemAccessException e) {
                Assertions.fail(e.getMessage());
            }
        });

        // POST
        // special case POST is converted to PUT
        for (String file : pathToFiles) {
            Mockito.when(this.s3Adapter.getPresignedUrl(file, Method.PUT, expiresInMinutes)).thenReturn(examplePresignedUrl);
        }

        final List<PresignedUrl> presignedUrls = this.fileOperations.getPresignedUrls(pathToFiles, Method.POST, expiresInMinutes);

        Assertions.assertEquals(3, presignedUrls.size());
        presignedUrls.forEach(presignedUrl -> {
            Assertions.assertEquals(presignedUrl.url(), examplePresignedUrl);
            Assertions.assertEquals(presignedUrl.action(), Method.PUT.toString());
            Assertions.assertTrue(pathToFiles.stream().anyMatch(file -> file.equals(presignedUrl.path())));
        });
    }

    @Test
    void getFileException() throws Exception {
        final String pathToFile = "folder/test.txt";
        final String pathToFolder = "folder";
        final int expiresInMinutes = 5;
        Mockito.when(this.s3Adapter.getFilePathsFromFolder(pathToFolder)).thenReturn(new HashSet<>());
        Assertions.assertThrows(FileExistenceException.class, () -> this.fileOperations.getFile(pathToFile, expiresInMinutes));
    }

    @Test
    void getFile() throws FileSystemAccessException, FileExistenceException {
        final String pathToFile = "folder/test.txt";
        final String pathToFolder = "folder";
        final int expiresInMinutes = 5;
        final String presignedUrl = "THE_PRESIGNED_URL";

        Mockito.when(this.s3Adapter.fileExists(pathToFile)).thenReturn(true);
        Mockito.when(this.s3Adapter.getFilePathsFromFolder(pathToFolder)).thenReturn(new HashSet<>(List.of(pathToFile)));
        Mockito.when(this.s3Adapter.getPresignedUrl(pathToFile, Method.GET, expiresInMinutes)).thenReturn(presignedUrl);

        final PresignedUrl result = this.fileOperations.getFile(pathToFile, expiresInMinutes);

        final PresignedUrl expected = new PresignedUrl(presignedUrl, pathToFile, "GET");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void saveFile() throws FileSystemAccessException {
        final String pathToFile = "folder/test.txt";
        final String pathToFolder = "folder";

        final FileData fileData = new FileData(pathToFile, 5);

        Mockito.when(this.s3Adapter.fileExists(pathToFile)).thenReturn(true);
        Mockito.when(this.s3Adapter.getFilePathsFromFolder(pathToFolder)).thenReturn(new HashSet<>(List.of(pathToFile)));
        Assertions.assertThrows(FileExistenceException.class, () -> this.fileOperations.saveFile(fileData));
        // happy path is tested in updateFile
    }

    @Test
    void updateFile() throws FileSystemAccessException {
        final String pathToFile = "folder/test.txt";

        final FileData fileData = new FileData(pathToFile, 5);

        // File not in Database
        this.fileOperations.updateFile(fileData);
        Mockito.verify(this.s3Adapter, Mockito.times(1)).getPresignedUrl(pathToFile, Method.PUT, fileData.expiresInMinutes());

        // File already in Database with older end of life
        Mockito.reset(this.s3Adapter);
        this.fileOperations.updateFile(fileData);
        Mockito.verify(this.s3Adapter, Mockito.times(1)).getPresignedUrl(pathToFile, Method.PUT, fileData.expiresInMinutes());

        // File already in Database with older and of life
        Mockito.reset(this.s3Adapter);
        this.fileOperations.updateFile(fileData);
        Mockito.verify(this.s3Adapter, Mockito.times(1)).getPresignedUrl(pathToFile, Method.PUT, fileData.expiresInMinutes());
    }

    @Test
    void deleteFileException() throws FileSystemAccessException {
        final String pathToFile = "folder/test.txt";
        final String pathToFolder = "folder";
        final int expiresInMinutes = 5;

        Mockito.reset(this.s3Adapter);
        Mockito.when(this.s3Adapter.getFilePathsFromFolder(pathToFolder)).thenReturn(new HashSet<>());
        Assertions.assertThrows(FileExistenceException.class, () -> this.fileOperations.deleteFile(pathToFile, expiresInMinutes));
    }

    @Test
    void deleteFile() throws FileSystemAccessException, FileExistenceException {
        final String pathToFile = "folder/test.txt";
        final String pathToFolder = "folder";
        final int expiresInMinutes = 5;

        Mockito.reset(this.s3Adapter);
        Mockito.when(this.s3Adapter.fileExists(pathToFile)).thenReturn(true);
        this.fileOperations.deleteFile(pathToFile, expiresInMinutes);
        Mockito.verify(this.s3Adapter, Mockito.times(1)).getPresignedUrl(pathToFile, Method.DELETE, expiresInMinutes);
        Mockito.verify(this.s3Adapter, Mockito.times(1)).fileExists(pathToFile);
    }

    @Test
    void pathToFolder() {
        assertThat(FileOperationsPresignedUrlUseCase.getPathToFolder("folder/file.txt")).isEqualTo("folder");
        assertThat(FileOperationsPresignedUrlUseCase.getPathToFolder("folder/subfolder/file.txt")).isEqualTo("folder/subfolder");
        assertThat(FileOperationsPresignedUrlUseCase.getPathToFolder("file.txt")).isEqualTo("");
        assertThat(FileOperationsPresignedUrlUseCase.getPathToFolder("")).isEqualTo("");
    }

}
