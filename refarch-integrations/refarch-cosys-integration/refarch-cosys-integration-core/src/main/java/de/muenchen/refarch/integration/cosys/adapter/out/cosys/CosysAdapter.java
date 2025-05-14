package de.muenchen.refarch.integration.cosys.adapter.out.cosys;

import de.muenchen.refarch.integration.cosys.api.GenerationApi;
import de.muenchen.refarch.integration.cosys.application.port.out.GenerateDocumentOutPort;
import de.muenchen.refarch.integration.cosys.configuration.CosysConfiguration;
import de.muenchen.refarch.integration.cosys.domain.exception.CosysException;
import de.muenchen.refarch.integration.cosys.domain.model.GenerateDocument;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class CosysAdapter implements GenerateDocumentOutPort {

    public static final String DATA_FILE_NAME = "data";
    public static final String MERGE_FILE_NAME = "merge";
    public static final String DOC_GEN_EXCEPTION_MESSAGE = "Document could not be created.";

    private final CosysConfiguration configuration;
    private final GenerationApi generationApi;

    /**
     * Generate a Document in Cosys
     *
     * @param generateDocument Data for generating documents
     * @return the generated document
     */
    @Override
    public Mono<InputStream> generateCosysDocument(final GenerateDocument generateDocument) {
        return this.generationApi.generatePdfWithResponseSpec(
                generateDocument.guid(),
                generateDocument.client(),
                generateDocument.role(),
                new ByteArrayResource(generateDocument.variables().toString().getBytes(StandardCharsets.UTF_8)),
                null,
                null,
                null,
                null,
                null,
                false,
                new ByteArrayResource(this.configuration.getMergeOptions()),
                null,
                null)
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new CosysException(DOC_GEN_EXCEPTION_MESSAGE)))
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new CosysException(DOC_GEN_EXCEPTION_MESSAGE)))
                .bodyToMono(InputStream.class);
    }

}
