package de.muenchen.refarch.s3.integration;

import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFileRepository;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFolderRepository;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;

@RequiredArgsConstructor
public class S3ExampleService {
    private static final String FILE_NAME = "test.txt";
    private static final String FILE_CONTENT = "test content";
    private static final int EXPIRE_MIN = 2;

    private final DocumentStorageFileRepository fileRepository;
    private final DocumentStorageFolderRepository folderRepository;
    private final String folderPrefix;

    protected void testS3() throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException, IOException {
        // upload file
        final String filePath = "%s/%s".formatted(folderPrefix, FILE_NAME);
        try (InputStream fileContent = new ByteArrayInputStream(FILE_CONTENT.getBytes(StandardCharsets.UTF_8))) {
            // FIXME
            // fileRepository.saveFileInputStream(filePath, fileContent, EXPIRE_MIN);
            fileRepository.saveFile(filePath, fileContent.readAllBytes(), EXPIRE_MIN);
        }
        // list file
        final Set<String> files = folderRepository.getAllFilesInFolderRecursively(folderPrefix);
        Assertions.assertTrue(files.contains(filePath));
        // get file
        try (InputStream fileContentGet = fileRepository.getFileInputStream(filePath, EXPIRE_MIN)) {
            final String fileContentGetString = new String(fileContentGet.readAllBytes());
            Assertions.assertEquals(FILE_CONTENT, fileContentGetString);
        }
        // delete file
        fileRepository.deleteFile(filePath, EXPIRE_MIN);
        // list file
        final Set<String> files2 = folderRepository.getAllFilesInFolderRecursively(folderPrefix);
        Assertions.assertTrue(files2.isEmpty());
    }
}
