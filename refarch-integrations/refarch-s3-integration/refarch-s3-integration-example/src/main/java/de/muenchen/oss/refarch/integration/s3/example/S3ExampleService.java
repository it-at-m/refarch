package de.muenchen.oss.refarch.integration.s3.example;

import de.muenchen.oss.refarch.integration.s3.application.port.out.S3OutPort;
import de.muenchen.oss.refarch.integration.s3.domain.exception.S3Exception;
import de.muenchen.oss.refarch.integration.s3.domain.model.FileMetadata;
import de.muenchen.oss.refarch.integration.s3.domain.model.FileReference;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ExampleService {
    private static final String BUCKET = "test-bucket";
    private static final String FOLDER = "test";
    private static final String PATH_TEMPLATE = "%s/%s";
    private static final String FILE_NAME = "test.txt";
    private static final String FILE_NAME_UNKNOWN = "test_unknown.txt";
    private static final String FILE_NAME_COPY = "test_copy.txt";
    private static final String FILE_NAME_COPY_TAGGED = "test_copy_tagged.txt";
    private static final String FILE_CONTENT = "test content";

    private final S3OutPort s3OutPort;

    @SuppressWarnings("PMD.CyclomaticComplexity")
    protected void testS3() throws IOException, S3Exception {
        log.info("Testing S3 integration");
        // upload file
        final String filePath = PATH_TEMPLATE.formatted(FOLDER, FILE_NAME);
        final FileReference fileReference = new FileReference(BUCKET, filePath);
        final byte[] content = FILE_CONTENT.getBytes(StandardCharsets.UTF_8);
        try (InputStream fileContent = new ByteArrayInputStream(content)) {
            s3OutPort.saveFile(fileReference, fileContent, content.length);
        }
        // upload file with unknown length
        final String filePathUnknown = PATH_TEMPLATE.formatted(FOLDER, FILE_NAME_UNKNOWN);
        final FileReference fileReferenceUnknown = new FileReference(BUCKET, filePathUnknown);
        try (InputStream fileContent = new ByteArrayInputStream(content)) {
            s3OutPort.saveFile(fileReferenceUnknown, fileContent);
        }
        // list file
        final List<FileMetadata> files = s3OutPort.getFilesWithPrefix(BUCKET, FOLDER, true);
        if (files.isEmpty() || !files.getFirst().path().equals(filePath)) {
            throw new IllegalStateException("Uploaded file not found in S3: " + filePath);
        }
        // tag file
        final Map<String, String> tags = Map.of("document-type", "example", "tenant", "muc");
        s3OutPort.setTags(fileReference, tags);
        if (!s3OutPort.getTags(fileReference).equals(tags)) {
            throw new IllegalStateException("Unexpected S3 tags for " + filePath);
        }
        // get file
        try (InputStream fileContentGet = s3OutPort.getFileContent(fileReference)) {
            final String fileContentGetString = new String(fileContentGet.readAllBytes(), StandardCharsets.UTF_8);
            if (!FILE_CONTENT.equals(fileContentGetString)) {
                throw new IllegalStateException("Unexpected S3 content for " + filePath);
            }
        }
        // get file unknown length
        try (InputStream fileContentGet = s3OutPort.getFileContent(fileReferenceUnknown)) {
            final String fileContentGetString = new String(fileContentGet.readAllBytes(), StandardCharsets.UTF_8);
            if (!FILE_CONTENT.equals(fileContentGetString)) {
                throw new IllegalStateException("Unexpected S3 content for " + filePath);
            }
        }
        // copy file with preserved tags
        final String filePathCopy = PATH_TEMPLATE.formatted(FOLDER, FILE_NAME_COPY);
        final FileReference copiedFileReference = new FileReference(BUCKET, filePathCopy);
        s3OutPort.copyFile(fileReference, copiedFileReference);
        if (!s3OutPort.getTags(copiedFileReference).equals(tags)) {
            throw new IllegalStateException("Unexpected copied tags for " + filePathCopy);
        }
        // copy file with overridden tags
        final String filePathCopyTagged = PATH_TEMPLATE.formatted(FOLDER, FILE_NAME_COPY_TAGGED);
        final FileReference copiedTaggedFileReference = new FileReference(BUCKET, filePathCopyTagged);
        s3OutPort.copyFile(fileReference, copiedTaggedFileReference, false);
        if (!s3OutPort.getTags(copiedTaggedFileReference).isEmpty()) {
            throw new IllegalStateException("Unexpected tags for tag-cleared copy " + filePathCopyTagged);
        }
        // delete file
        s3OutPort.deleteFile(fileReference);
        s3OutPort.deleteFile(fileReferenceUnknown);
        s3OutPort.deleteFile(copiedFileReference);
        s3OutPort.deleteFile(copiedTaggedFileReference);
        // list file
        final List<FileMetadata> files2 = s3OutPort.getFilesWithPrefix(BUCKET, FOLDER, true);
        if (!files2.isEmpty()) {
            throw new IllegalStateException("S3 folder not empty after delete: " + FOLDER);
        }
        log.info("Finished testing");
    }
}
