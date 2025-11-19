package de.muenchen.refarch.integration.cosys.adapter.out.cosys;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.muenchen.refarch.integration.cosys.api.GenerationApi;
import de.muenchen.refarch.integration.cosys.application.port.out.GenerateDocumentOutPort;
import de.muenchen.refarch.integration.cosys.configuration.CosysConfiguration;
import de.muenchen.refarch.integration.cosys.domain.exception.CosysException;
import de.muenchen.refarch.integration.cosys.domain.model.GenerateDocument;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatusCode;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class CosysAdapter implements GenerateDocumentOutPort {

    public static final String DOC_GEN_EXCEPTION_MESSAGE = "Document could not be created.";

    private final CosysConfiguration configuration;
    private final GenerationApi generationApi;
    private final ObjectMapper objectMapper;

    /**
     * Generate a Document in Cosys
     *
     * @param generateDocument Data for generating documents
     * @return the generated document
     */
    @Override
    public Mono<InputStream> generateCosysDocument(final GenerateDocument generateDocument) {
        final AbstractResource data = new NamedByteArrayResource(generateDocument.variables().toString().getBytes(StandardCharsets.UTF_8), "data.json");
        final AbstractResource mergeOptions = this.getMergeOptions();
        return this.generationApi.generatePdfWithResponseSpec(
                generateDocument.guid(),
                generateDocument.client(),
                generateDocument.role(),
                data,
                null,
                null,
                null,
                null,
                null,
                false,
                mergeOptions,
                null,
                null)
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new CosysException(DOC_GEN_EXCEPTION_MESSAGE)))
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new CosysException(DOC_GEN_EXCEPTION_MESSAGE)))
                .bodyToMono(DataBuffer.class).map(DataBuffer::asInputStream);
    }

    private AbstractResource getMergeOptions() {
        final AbstractResource mergeOptions;
        if (this.configuration.getMergeOptions() != null) {
            try {
                final String mergeOptionsString = objectMapper.writeValueAsString(this.configuration.getMergeOptions());
                mergeOptions = new NamedByteArrayResource(mergeOptionsString.getBytes(StandardCharsets.UTF_8), "merge.json");
            } catch (final JsonProcessingException e) {
                throw new IllegalArgumentException("Cosys merge options are not valid.", e);
            }
        } else {
            mergeOptions = null;
        }
        return mergeOptions;
    }

}
