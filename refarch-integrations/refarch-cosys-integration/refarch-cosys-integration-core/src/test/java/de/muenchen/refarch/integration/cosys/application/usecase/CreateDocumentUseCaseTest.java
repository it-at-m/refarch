package de.muenchen.refarch.integration.cosys.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.muenchen.refarch.integration.cosys.application.port.out.GenerateDocumentOutPort;
import de.muenchen.refarch.integration.cosys.domain.exception.CosysException;
import de.muenchen.refarch.integration.cosys.domain.model.GenerateDocument;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

class CreateDocumentUseCaseTest {

    private final GenerateDocumentOutPort generateDocumentOutPort = mock(GenerateDocumentOutPort.class);

    private final GenerateDocument generateDocument = new GenerateDocument("Client", "Role", "guid", new ObjectMapper().readTree("{\"key1\":\"value\"}"));

    protected CreateDocumentUseCaseTest() throws JsonProcessingException {
    }

    @Test
    @SuppressWarnings("PMD.CloseResource")
    void createDocument() throws CosysException, IOException {
        when(generateDocumentOutPort.generateCosysDocument(any())).thenReturn(Mono.just(new ByteArrayInputStream("Document".getBytes(StandardCharsets.UTF_8))));

        final CreateDocumentUseCase useCase = new CreateDocumentUseCase(generateDocumentOutPort);
        final InputStream response = useCase.createDocument(generateDocument);

        verify(generateDocumentOutPort).generateCosysDocument(generateDocument);
        verifyNoMoreInteractions(generateDocumentOutPort);
        assertEquals("Document", new String(response.readAllBytes(), StandardCharsets.UTF_8));
    }
}
