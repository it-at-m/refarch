package de.muenchen.refarch.integration.cosys.adapter.out.cosys;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.muenchen.refarch.integration.cosys.api.GenerationApi;
import de.muenchen.refarch.integration.cosys.configuration.CosysConfiguration;
import de.muenchen.refarch.integration.cosys.domain.model.GenerateDocument;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

class CosysAdapterTest {
    private final CosysConfiguration configuration = mock(CosysConfiguration.class);
    private final GenerationApi generationApi = mock(GenerationApi.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CosysAdapter cosysAdapter = new CosysAdapter(configuration, generationApi, objectMapper);

    @Test
    @SuppressWarnings("PMD.CloseResource")
    void testGenerateCosysDocumentSuccess() throws IOException {
        // given
        final GenerateDocument generateDocument = generateDocument();
        final byte[] responseBody = "Response".getBytes();
        final DataBuffer dataBuffer = new DefaultDataBufferFactory().allocateBuffer(responseBody.length);
        dataBuffer.write(responseBody);
        final WebClient.ResponseSpec responseSpecMock = mock(WebClient.ResponseSpec.class);
        when(responseSpecMock.onStatus(any(), any())).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(DataBuffer.class)).thenReturn(Mono.just(dataBuffer));
        final ArgumentCaptor<AbstractResource> dataFileCaptor = ArgumentCaptor.forClass(AbstractResource.class);
        final ArgumentCaptor<AbstractResource> mergeFileCaptor = ArgumentCaptor.forClass(AbstractResource.class);

        when(generationApi.generatePdfWithResponseSpec(any(), any(), any(), dataFileCaptor.capture(), any(), any(), any(), any(), any(), any(),
                mergeFileCaptor.capture(), any(), any()))
                .thenReturn(responseSpecMock);

        when(configuration.getMergeOptions()).thenReturn(Map.of("mergedata", "test"));

        //when
        final InputStream response = cosysAdapter.generateCosysDocument(generateDocument).block();

        //then
        assert response != null;
        assertEquals("Response", new String(response.readAllBytes()));
        verify(generationApi).generatePdfWithResponseSpec(
                generateDocument.guid(),
                generateDocument.client(),
                generateDocument.role(),
                dataFileCaptor.getValue(),
                null,
                null,
                null,
                null,
                null,
                false,
                mergeFileCaptor.getValue(),
                null,
                null);
    }

    private GenerateDocument generateDocument() {
        try {
            return new GenerateDocument(
                    "client",
                    "role",
                    "guid",
                    new ObjectMapper().readTree("{\"name\":\"John\", \"age\":30}"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
