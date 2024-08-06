package de.muenchen.refarch.email.integration.adapter.out.s3;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.muenchen.refarch.email.integration.domain.exception.LoadAttachmentError;
import de.muenchen.refarch.email.model.FileAttachment;
import de.muenchen.refarch.s3.integration.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.s3.integration.client.exception.DocumentStorageException;
import de.muenchen.refarch.s3.integration.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.s3.integration.client.repository.DocumentStorageFileRepository;
import de.muenchen.refarch.s3.integration.client.repository.DocumentStorageFolderRepository;
import de.muenchen.refarch.s3.integration.client.repository.transfer.S3FileTransferRepository;
import de.muenchen.refarch.s3.integration.client.service.FileService;
import de.muenchen.refarch.s3.integration.client.service.S3StorageUrlProvider;
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

    private final S3FileTransferRepository s3FileTransferRepository = mock(S3FileTransferRepository.class);
    private final DocumentStorageFileRepository documentStorageFileRepository = mock(DocumentStorageFileRepository.class);
    private final DocumentStorageFolderRepository documentStorageFolderRepository = mock(DocumentStorageFolderRepository.class);
    private final S3StorageUrlProvider s3DomainService = mock(S3StorageUrlProvider.class);
    private final FileService fileService = new FileService(null, DataSize.ofMegabytes(50), DataSize.ofMegabytes(110));

    private S3Adapter s3Adapter;

    @BeforeEach
    void setup() {
        s3Adapter = new S3Adapter(s3FileTransferRepository, documentStorageFileRepository, documentStorageFolderRepository, fileService, s3DomainService);
    }

    @Test
    void testLoadAttachment_DocumentStorageException()
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String path = "path/to/some-file.txt";
        final String context = "fileContext";
        final String fullPath = context + "/" + path;

        // DocumentStorageException
        when(documentStorageFileRepository.getFile(eq(fullPath), anyInt(), isNull()))
                .thenThrow(new DocumentStorageException("Some error", new RuntimeException("Some error")));
        assertThatThrownBy(() -> s3Adapter.loadAttachments(context, List.of(path)))
                .isInstanceOf(LoadAttachmentError.class);
    }

    @Test
    void testLoadAttachment_Success() throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final Map<String, String> files = Map.of(
                "digiwf_logo.png", "image/png",
                "test-pdf.pdf", "application/pdf",
                "test-word.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        );

        for (final Map.Entry<String, String> file : files.entrySet()) {
            try {
                final String path = "files/" + file.getKey();
                final byte[] testFile = new ClassPathResource(path).getInputStream().readAllBytes();
                when(documentStorageFileRepository.getFile(anyString(), anyInt(), isNull())).thenReturn(testFile);

                final List<FileAttachment> fileAttachment = this.s3Adapter.loadAttachments("fileContext", List.of(path));

                assertThat(Arrays.equals(testFile, fileAttachment.getFirst().getFile().getInputStream().readAllBytes())).isTrue();
                assertThat(file.getKey()).isEqualTo(fileAttachment.getFirst().getFileName());
                assertThat(file.getValue()).isEqualTo(fileAttachment.getFirst().getFile().getContentType());
            } catch (final IOException e) {
                log.warn("Could not read file: {}", file);
                fail(e.getMessage());
            }
        }
    }

}
