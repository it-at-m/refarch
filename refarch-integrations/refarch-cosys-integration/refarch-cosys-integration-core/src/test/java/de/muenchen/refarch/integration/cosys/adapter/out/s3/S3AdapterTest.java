package de.muenchen.refarch.integration.cosys.adapter.out.s3;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.client.exception.FileSizeValidationException;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFileRepository;
import de.muenchen.refarch.integration.s3.client.repository.transfer.S3FileTransferRepository;
import de.muenchen.refarch.integration.s3.client.service.FileValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.unit.DataSize;

class S3AdapterTest {
    private static final String DATA = "In CoSys generiertes Dokument";
    private static final byte[] DATA_AS_BYTE_ARRAY = DATA.getBytes();
    private static final DataSize ALLOWED_FILE_SIZE = DataSize.ofBytes(DATA_AS_BYTE_ARRAY.length);
    private static final DataSize ALLOWED_BATCH_SIZE = DataSize.ofMegabytes(110);
    private static final byte[] TOO_LARGE_FILE = (DATA + "!").getBytes(); // 1 Mbyte over allowed

    private final S3FileTransferRepository s3FileTransferRepository = mock(S3FileTransferRepository.class);
    private final DocumentStorageFileRepository documentStorageFileRepository = mock(DocumentStorageFileRepository.class);
    private final FileValidationService fileValidationService = new FileValidationService(null, ALLOWED_FILE_SIZE, ALLOWED_BATCH_SIZE);
    private S3Adapter s3Adapter;

    @BeforeEach
    void setup() {
        s3Adapter = new S3Adapter(s3FileTransferRepository, documentStorageFileRepository, fileValidationService);
    }

    @Test
    void saveDocumentInStorage() throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        s3Adapter.saveDocumentInStorage("test/filePath.txt", DATA_AS_BYTE_ARRAY);
        verify(documentStorageFileRepository).saveFile("test/filePath.txt", DATA_AS_BYTE_ARRAY, 1);
        verifyNoMoreInteractions(s3FileTransferRepository);

    }

    @Test
    void saveDocumentInStorageWithThrowsDocumentStorageException()
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        // v2
        doThrow(new DocumentStorageException("DocumentStorageClientErrorException", new Exception())).when(documentStorageFileRepository)
                .saveFile(anyString(), any(), eq(1));
        DocumentStorageException documentStorageException = assertThrows(DocumentStorageException.class,
                () -> s3Adapter.saveDocumentInStorage("filePath.txt", DATA_AS_BYTE_ARRAY));

        String expectedMessage = "Document could not be saved.";
        String actualMessage = documentStorageException.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void testSaveDocumentInStorageThrowsBpmnErrorForInvalidFileSize() {
        String expectedMessage = String.format("Invalid file size %d MB. Allowed are %d MB.", DataSize.ofBytes(TOO_LARGE_FILE.length).toMegabytes(),
                ALLOWED_FILE_SIZE.toMegabytes());

        assertThatThrownBy(() -> s3Adapter.saveDocumentInStorage("filePath.txt", TOO_LARGE_FILE))
                .isInstanceOf(FileSizeValidationException.class)
                .extracting("message")
                .isEqualTo(expectedMessage);
    }

}
