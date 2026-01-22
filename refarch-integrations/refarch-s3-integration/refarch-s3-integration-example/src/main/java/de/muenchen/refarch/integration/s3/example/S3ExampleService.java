package de.muenchen.refarch.integration.s3.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.muenchen.refarch.integration.s3.application.port.in.FileOperationsInPort;
import de.muenchen.refarch.integration.s3.application.port.in.FolderOperationsInPort;
import de.muenchen.refarch.integration.s3.domain.exception.S3Exception;
import de.muenchen.refarch.integration.s3.domain.model.FileMetadata;
import de.muenchen.refarch.integration.s3.domain.model.FileReference;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ExampleService {
    private static final String BUCKET = "test-bucket";
    private static final String FOLDER = "test";
    private static final String FILE_NAME = "test.txt";
    private static final String FILE_CONTENT = "test content";

    private final FileOperationsInPort fileOperationsInPort;
    private final FolderOperationsInPort folderOperationsInPort;

    protected void testS3() throws IOException, S3Exception {
        log.info("Testing S3 inetgration");
        // upload file
        final String filePath = "%s/%s".formatted(FOLDER, FILE_NAME);
        final FileReference fileReference = new FileReference(BUCKET, filePath);
        final byte[] content = FILE_CONTENT.getBytes(StandardCharsets.UTF_8);
        try (InputStream fileContent = new ByteArrayInputStream(content)) {
            fileOperationsInPort.saveFile(fileReference, fileContent, content.length);
        }
        // list file
        final List<FileMetadata> files = folderOperationsInPort.getFilesInFolder(BUCKET, FOLDER, false);
        assertEquals(1, files.size());
        assertEquals(filePath, files.getFirst().path());
        // get file
        try (InputStream fileContentGet = fileOperationsInPort.getFileContent(fileReference)) {
            final String fileContentGetString = new String(fileContentGet.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(FILE_CONTENT, fileContentGetString);
        }
        // delete file
        fileOperationsInPort.deleteFile(fileReference);
        // list file
        final List<FileMetadata> files2 = folderOperationsInPort.getFilesInFolder(BUCKET, FOLDER, false);
        Assertions.assertTrue(files2.isEmpty());
        log.info("Finished testing");
    }
}
