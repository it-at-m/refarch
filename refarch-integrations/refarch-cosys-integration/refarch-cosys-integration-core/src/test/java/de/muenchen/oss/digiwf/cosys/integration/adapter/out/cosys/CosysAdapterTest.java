package de.muenchen.oss.digiwf.cosys.integration.adapter.out.cosys;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.muenchen.oss.digiwf.cosys.integration.configuration.CosysConfiguration;
import de.muenchen.oss.digiwf.cosys.integration.domain.exception.CosysException;
import de.muenchen.oss.digiwf.cosys.integration.domain.model.GenerateDocument;
import de.muenchen.refarch.integration.cosys.api.GenerationApi;
import java.io.File;
import java.io.IOException;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

class CosysAdapterTest {

    @Mock
    private CosysConfiguration configuration;

    @Mock
    private GenerationApi generationApi;

    @InjectMocks
    private CosysAdapter cosysAdapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateCosysDocument_Success() throws IOException, CosysException {
        // given
        val generateDocument = generateDocument();
        val response = "Response".getBytes();
        val responseSpecMock = Mockito.mock(WebClient.ResponseSpec.class);
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
                generateDocument.getGuid(),
                generateDocument.getClient(),
                generateDocument.getRole(),
                dataFileCaptor.getValue(),
                null,
                null,
                null,
                null,
                null,
                false,
                mergeFileCaptor.getValue(),
                null,
                null
        );
    }

    private GenerateDocument generateDocument() {
        try {
            return new GenerateDocument(
                    "client",
                    "role",
                    "guid",
                    new ObjectMapper().readTree("{\"name\":\"John\", \"age\":30}")
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
