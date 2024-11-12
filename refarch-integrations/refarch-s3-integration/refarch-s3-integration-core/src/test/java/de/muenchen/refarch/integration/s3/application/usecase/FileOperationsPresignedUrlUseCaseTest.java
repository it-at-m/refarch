package de.muenchen.refarch.integration.s3.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    public static final String TEST_TXT_PATH = "folder/test.txt";
    public static final String EXAMPLE_PRESIGNED_URL = "some-presigned-url";
    public static final String PATH_TO_FOLDER = "folder";
    @Mock
    private S3Adapter s3Adapter;

    private FileOperationsPresignedUrlInPort fileOperations;

    @BeforeEach
    void beforeEach() {
        this.fileOperations = new FileOperationsPresignedUrlUseCase(this.s3Adapter);
        Mockito.reset(this.s3Adapter);
    }

    @Test
    void testGetPresignedUrl() {
        final int expiresInMinutes = 5;
        final List<Method> actions = List.of(Method.GET, Method.POST, Method.PUT, Method.DELETE);

        for (final Method action : actions) {
            try {
                Mockito.when(this.s3Adapter.getPresignedUrl(TEST_TXT_PATH, action, expiresInMinutes)).thenReturn(EXAMPLE_PRESIGNED_URL);

                final PresignedUrl presignedUrl = fileOperations.getPresignedUrl(TEST_TXT_PATH, action, expiresInMinutes);

                assertEquals(EXAMPLE_PRESIGNED_URL, presignedUrl.url());
                assertEquals(action.toString(), presignedUrl.action());
                assertEquals(TEST_TXT_PATH, presignedUrl.path());
            } catch (final FileSystemAccessException e) {
                Assertions.fail(e.getMessage());
            }
        }

    }

    @Test
    void testGetPresignedUrlForFile() throws FileSystemAccessException {
        final int expiresInMinutes = 5;
        final List<Method> actions = List.of(Method.GET, Method.PUT, Method.DELETE);

        // GET, PUT, DELETE
        actions.forEach(action -> {
            try {
                Mockito.when(this.s3Adapter.fileExists(TEST_TXT_PATH)).thenReturn(true);
                Mockito.when(this.s3Adapter.getFilePathsFromFolder(TEST_TXT_PATH)).thenReturn(Set.of(TEST_TXT_PATH));
                Mockito.when(this.s3Adapter.getPresignedUrl(TEST_TXT_PATH, action, expiresInMinutes)).thenReturn(EXAMPLE_PRESIGNED_URL);

                final List<PresignedUrl> presignedUrls = this.fileOperations.getPresignedUrls(List.of(TEST_TXT_PATH), action, expiresInMinutes);

                assertEquals(1, presignedUrls.size());
                presignedUrls.forEach(presignedUrl -> {
                    assertEquals(presignedUrl.url(), EXAMPLE_PRESIGNED_URL);
                    assertEquals(presignedUrl.action(), action.toString());
                    assertEquals(presignedUrl.path(), TEST_TXT_PATH);
                });
                Mockito.reset();
            } catch (final FileExistenceException | FileSystemAccessException e) {
                Assertions.fail(e.getMessage());
            }
        });

        // POST
        // special case POST is converted to PUT
        Mockito.when(this.s3Adapter.getPresignedUrl(TEST_TXT_PATH, Method.PUT, expiresInMinutes)).thenReturn(EXAMPLE_PRESIGNED_URL);

        final List<PresignedUrl> presignedUrls = this.fileOperations.getPresignedUrls(List.of(TEST_TXT_PATH), Method.POST, expiresInMinutes);

        assertEquals(1, presignedUrls.size());
        presignedUrls.forEach(presignedUrl -> {
            assertEquals(presignedUrl.url(), EXAMPLE_PRESIGNED_URL);
            assertEquals(presignedUrl.action(), Method.PUT.toString());
            assertEquals(presignedUrl.path(), TEST_TXT_PATH);
        });
    }

    @Test
    void testGetPresignedUrlsForDirectory() throws FileSystemAccessException {
        final String pathToDirectory = "folder/";
        final Set<String> files = Set.of(TEST_TXT_PATH, "folder/test1.txt");
        final int expiresInMinutes = 5;
        final List<Method> actions = List.of(Method.GET, Method.PUT, Method.DELETE);

        // GET, PUT, DELETE
        actions.forEach(action -> {
            try {
                Mockito.when(this.s3Adapter.getFilePathsFromFolder(pathToDirectory)).thenReturn(files);
                for (final String file : files) {
                    Mockito.when(this.s3Adapter.getPresignedUrl(file, action, expiresInMinutes)).thenReturn(EXAMPLE_PRESIGNED_URL);
                }

                final List<PresignedUrl> presignedUrls = this.fileOperations.getPresignedUrls(List.of(pathToDirectory), action, expiresInMinutes);

                assertEquals(2, presignedUrls.size());
                presignedUrls.forEach(presignedUrl -> {
                    assertEquals(presignedUrl.url(), EXAMPLE_PRESIGNED_URL);
                    assertEquals(presignedUrl.action(), action.toString());
                    assertTrue(files.stream().anyMatch(file -> file.equals(presignedUrl.path())));
                });
                Mockito.reset();
            } catch (final FileExistenceException | FileSystemAccessException e) {
                Assertions.fail(e.getMessage());
            }
        });

        // POST
        // special case POST is converted to PUT
        Mockito.when(this.s3Adapter.getPresignedUrl(pathToDirectory, Method.PUT, expiresInMinutes)).thenReturn(EXAMPLE_PRESIGNED_URL);

        final List<PresignedUrl> presignedUrls = this.fileOperations.getPresignedUrls(List.of(pathToDirectory), Method.POST, expiresInMinutes);

        assertEquals(1, presignedUrls.size());
        presignedUrls.forEach(presignedUrl -> {
            assertEquals(presignedUrl.url(), EXAMPLE_PRESIGNED_URL);
            assertEquals(presignedUrl.action(), Method.PUT.toString());
            assertEquals(presignedUrl.path(), pathToDirectory);
        });
    }

    @Test
    void testGetPresignedUrlsForMultipleFiles() throws FileSystemAccessException {
        final List<String> pathToFiles = List.of("folder/first.txt", "folder/second.txt", "folder/third.txt");
        final int expiresInMinutes = 5;
        final List<Method> actions = List.of(Method.GET, Method.PUT, Method.DELETE);

        // GET, PUT, DELETE
        actions.forEach(action -> {
            try {
                for (final String file : pathToFiles) {
                    Mockito.when(this.s3Adapter.getFilePathsFromFolder(file)).thenReturn(Set.of(file));
                    Mockito.when(this.s3Adapter.getPresignedUrl(file, action, expiresInMinutes)).thenReturn(EXAMPLE_PRESIGNED_URL);
                }

                final List<PresignedUrl> presignedUrls = this.fileOperations.getPresignedUrls(pathToFiles, action, expiresInMinutes);

                assertEquals(3, presignedUrls.size());
                presignedUrls.forEach(presignedUrl -> {
                    assertEquals(presignedUrl.url(), EXAMPLE_PRESIGNED_URL);
                    assertEquals(presignedUrl.action(), action.toString());
                });
                Mockito.reset();
            } catch (final FileExistenceException | FileSystemAccessException e) {
                Assertions.fail(e.getMessage());
            }
        });

        // POST
        // special case POST is converted to PUT
        for (final String file : pathToFiles) {
            Mockito.when(this.s3Adapter.getPresignedUrl(file, Method.PUT, expiresInMinutes)).thenReturn(EXAMPLE_PRESIGNED_URL);
        }

        final List<PresignedUrl> presignedUrls = this.fileOperations.getPresignedUrls(pathToFiles, Method.POST, expiresInMinutes);

        assertEquals(3, presignedUrls.size());
        presignedUrls.forEach(presignedUrl -> {
            assertEquals(presignedUrl.url(), EXAMPLE_PRESIGNED_URL);
            assertEquals(presignedUrl.action(), Method.PUT.toString());
            assertTrue(pathToFiles.stream().anyMatch(file -> file.equals(presignedUrl.path())));
        });
    }

    @Test
    void testGetFileException() throws Exception {
        final int expiresInMinutes = 5;
        Mockito.when(this.s3Adapter.getFilePathsFromFolder(PATH_TO_FOLDER)).thenReturn(new HashSet<>());
        assertThrows(FileExistenceException.class, () -> this.fileOperations.getFile(TEST_TXT_PATH, expiresInMinutes));
    }

    @Test
    void testGetFile() throws FileSystemAccessException {
        final int expiresInMinutes = 5;
        final String presignedUrl = "THE_PRESIGNED_URL";

        Mockito.when(this.s3Adapter.fileExists(TEST_TXT_PATH)).thenReturn(true);
        Mockito.when(this.s3Adapter.getFilePathsFromFolder(PATH_TO_FOLDER)).thenReturn(new HashSet<>(List.of(TEST_TXT_PATH)));
        Mockito.when(this.s3Adapter.getPresignedUrl(TEST_TXT_PATH, Method.GET, expiresInMinutes)).thenReturn(presignedUrl);

        final PresignedUrl result = this.fileOperations.getFile(TEST_TXT_PATH, expiresInMinutes);

        final PresignedUrl expected = new PresignedUrl(presignedUrl, TEST_TXT_PATH, "GET");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void saveFile() throws FileSystemAccessException {
        final FileData fileData = new FileData(TEST_TXT_PATH, 5);

        Mockito.when(this.s3Adapter.fileExists(TEST_TXT_PATH)).thenReturn(true);
        Mockito.when(this.s3Adapter.getFilePathsFromFolder(PATH_TO_FOLDER)).thenReturn(new HashSet<>(List.of(TEST_TXT_PATH)));
        assertThrows(FileExistenceException.class, () -> this.fileOperations.saveFile(fileData));
        // happy path is tested in updateFile
    }

    @Test
    void updateFile() throws FileSystemAccessException {
        final FileData fileData = new FileData(TEST_TXT_PATH, 5);

        // File not in Database
        this.fileOperations.updateFile(fileData);
        Mockito.verify(this.s3Adapter, Mockito.times(1)).getPresignedUrl(TEST_TXT_PATH, Method.PUT, fileData.expiresInMinutes());

        // File already in Database with older end of life
        Mockito.reset(this.s3Adapter);
        this.fileOperations.updateFile(fileData);
        Mockito.verify(this.s3Adapter, Mockito.times(1)).getPresignedUrl(TEST_TXT_PATH, Method.PUT, fileData.expiresInMinutes());

        // File already in Database with older and of life
        Mockito.reset(this.s3Adapter);
        this.fileOperations.updateFile(fileData);
        Mockito.verify(this.s3Adapter, Mockito.times(1)).getPresignedUrl(TEST_TXT_PATH, Method.PUT, fileData.expiresInMinutes());
    }

    @Test
    void deleteFileException() throws FileSystemAccessException {
        final int expiresInMinutes = 5;

        Mockito.reset(this.s3Adapter);
        Mockito.when(this.s3Adapter.getFilePathsFromFolder(PATH_TO_FOLDER)).thenReturn(new HashSet<>());
        assertThrows(FileExistenceException.class, () -> this.fileOperations.deleteFile(TEST_TXT_PATH, expiresInMinutes));
    }

    @Test
    void deleteFile() throws FileSystemAccessException {
        final int expiresInMinutes = 5;

        Mockito.reset(this.s3Adapter);
        Mockito.when(this.s3Adapter.fileExists(TEST_TXT_PATH)).thenReturn(true);
        this.fileOperations.deleteFile(TEST_TXT_PATH, expiresInMinutes);
        Mockito.verify(this.s3Adapter, Mockito.times(1)).getPresignedUrl(TEST_TXT_PATH, Method.DELETE, expiresInMinutes);
        Mockito.verify(this.s3Adapter, Mockito.times(1)).fileExists(TEST_TXT_PATH);
    }

    @Test
    void pathToFolder() {
        assertThat(FileOperationsPresignedUrlUseCase.getPathToFolder("folder/file.txt")).isEqualTo("folder");
        assertThat(FileOperationsPresignedUrlUseCase.getPathToFolder("folder/subfolder/file.txt")).isEqualTo("folder/subfolder");
        assertThat(FileOperationsPresignedUrlUseCase.getPathToFolder("file.txt")).isEqualTo("");
        assertThat(FileOperationsPresignedUrlUseCase.getPathToFolder("")).isEqualTo("");
    }

}
