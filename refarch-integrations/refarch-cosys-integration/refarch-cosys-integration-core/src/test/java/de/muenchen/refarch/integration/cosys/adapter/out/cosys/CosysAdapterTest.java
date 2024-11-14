package de.muenchen.refarch.integration.cosys.adapter.out.cosys;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.muenchen.refarch.integration.cosys.api.GenerationApi;
import de.muenchen.refarch.integration.cosys.configuration.CosysConfiguration;
import de.muenchen.refarch.integration.cosys.domain.exception.CosysException;
import de.muenchen.refarch.integration.cosys.domain.model.GenerateDocument;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

class CosysAdapterTest {
    private final CosysConfiguration configuration = mock(CosysConfiguration.class);
    private final GenerationApi generationApi = mock(GenerationApi.class);
    private final CosysAdapter cosysAdapter = new CosysAdapter(configuration, generationApi);

    @Test
    void testGenerateCosysDocumentSuccess() throws IOException, CosysException {
        // given
        final GenerateDocument generateDocument = generateDocument();
        final byte[] response = "Response".getBytes();
        final WebClient.ResponseSpec responseSpecMock = mock(WebClient.ResponseSpec.class);
        when(responseSpecMock.onStatus(any(), any())).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(byte[].class)).thenReturn(Mono.just(response));
        final ArgumentCaptor<File> dataFileCaptor = ArgumentCaptor.forClass(File.class);
        final ArgumentCaptor<File> mergeFileCaptor = ArgumentCaptor.forClass(File.class);

        when(generationApi.generatePdfWithResponseSpec(any(), any(), any(), dataFileCaptor.capture(), any(), any(), any(), any(), any(), any(),
                mergeFileCaptor.capture(), any(), any()))
                        .thenReturn(responseSpecMock);

        when(configuration.getMergeOptions()).thenReturn("mergedata".getBytes());

        //when
        cosysAdapter.generateCosysDocument(generateDocument);

        //then

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
