package de.muenchen.refarch.integration.cosys.application.usecase;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.muenchen.refarch.integration.cosys.application.port.out.GenerateDocumentOutPort;
import de.muenchen.refarch.integration.cosys.application.port.out.SaveFileToStorageOutPort;
import de.muenchen.refarch.integration.cosys.domain.exception.CosysException;
import de.muenchen.refarch.integration.cosys.domain.model.GenerateDocument;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

class CreateDocumentUseCaseTest {

    private final GenerateDocumentOutPort generateDocumentOutPort = mock(GenerateDocumentOutPort.class);

    private final SaveFileToStorageOutPort saveFileToStorageOutPort = mock(SaveFileToStorageOutPort.class);

    private final GenerateDocument generateDocument = new GenerateDocument("Client", "Role", "guid", new ObjectMapper().readTree("{\"key1\":\"value\"}"));

    CreateDocumentUseCaseTest() throws JsonProcessingException {
    }

    @Test
    void createDocument() throws DocumentStorageException, CosysException {
        when(generateDocumentOutPort.generateCosysDocument(any())).thenReturn(Mono.just("Document".getBytes()));

        final CreateDocumentUseCase useCase = new CreateDocumentUseCase(saveFileToStorageOutPort, generateDocumentOutPort);
        useCase.createDocument(generateDocument, "path.file");

        verify(generateDocumentOutPort).generateCosysDocument(generateDocument);
        verifyNoMoreInteractions(generateDocumentOutPort);

        verify(saveFileToStorageOutPort).saveDocumentInStorage("path.file", "Document".getBytes());
        verifyNoMoreInteractions(saveFileToStorageOutPort);
    }
}
