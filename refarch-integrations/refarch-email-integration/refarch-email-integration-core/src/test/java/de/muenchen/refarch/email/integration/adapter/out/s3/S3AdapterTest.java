package de.muenchen.refarch.email.integration.adapter.out.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.muenchen.refarch.email.integration.domain.exception.LoadAttachmentError;
import de.muenchen.refarch.email.model.FileAttachment;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFileRepository;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFolderRepository;
import de.muenchen.refarch.integration.s3.client.service.FileValidationService;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.unit.DataSize;

@Slf4j
class S3AdapterTest {
    private final DocumentStorageFileRepository documentStorageFileRepository = mock(DocumentStorageFileRepository.class);
    private final DocumentStorageFolderRepository documentStorageFolderRepository = mock(DocumentStorageFolderRepository.class);
    private final FileValidationService fileValidationService = new FileValidationService(null, DataSize.ofMegabytes(50), DataSize.ofMegabytes(110));

    private S3Adapter s3Adapter;

    @BeforeEach
    void setup() {
        s3Adapter = new S3Adapter(documentStorageFileRepository, documentStorageFolderRepository, fileValidationService);
    }

    @Test
    void testLoadAttachmentDocumentStorageException()
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String path = "path/to/some-file.txt";

        // DocumentStorageException
        when(documentStorageFileRepository.getFile(eq(path), anyInt()))
                .thenThrow(new DocumentStorageException("Some error", new RuntimeException("Some error")));
        assertThatThrownBy(() -> s3Adapter.loadAttachments(List.of(path)))
                .isInstanceOf(LoadAttachmentError.class);
    }

    @Test
    void testLoadAttachmentSuccess() throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final Map<String, String> files = Map.of(
                "test-logo.png", "image/png",
                "test-pdf.pdf", "application/pdf",
                "test-word.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        for (final Map.Entry<String, String> file : files.entrySet()) {
            try {
                final String path = "files/" + file.getKey();
                final byte[] testFile = new ClassPathResource(path).getInputStream().readAllBytes();
                when(documentStorageFileRepository.getFile(anyString(), anyInt())).thenReturn(testFile);

                final List<FileAttachment> fileAttachment = this.s3Adapter.loadAttachments(List.of(path));

                assertThat(Arrays.equals(testFile, fileAttachment.getFirst().file().getInputStream().readAllBytes())).isTrue();
                assertThat(file.getKey()).isEqualTo(fileAttachment.getFirst().fileName());
                assertThat(file.getValue()).isEqualTo(fileAttachment.getFirst().file().getContentType());
            } catch (final IOException e) {
                log.warn("Could not read file: {}", file);
                fail(e.getMessage());
            }
        }
    }

}
