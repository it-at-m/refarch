package de.muenchen.refarch.integration.cosys.adapter.out.cosys;

import de.muenchen.refarch.integration.cosys.api.GenerationApi;
import de.muenchen.refarch.integration.cosys.application.port.out.GenerateDocumentOutPort;
import de.muenchen.refarch.integration.cosys.configuration.CosysConfiguration;
import de.muenchen.refarch.integration.cosys.domain.exception.CosysException;
import de.muenchen.refarch.integration.cosys.domain.model.GenerateDocument;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class CosysAdapter implements GenerateDocumentOutPort {

    public static final String DATA_FILE_NAME = "data";
    public static final String MERGE_FILE_NAME = "merge";

    private final CosysConfiguration configuration;
    private final GenerationApi generationApi;

    /**
     * Generate a Document in Cosys
     *
     * @param generateDocument Data for generating documents
     * @return the generated document
     */
    @Override
    public Mono<byte[]> generateCosysDocument(final GenerateDocument generateDocument) throws CosysException {
        try {
            return this.generationApi.generatePdfWithResponseSpec(
                    generateDocument.guid(),
                    generateDocument.client(),
                    generateDocument.role(),
                    FileUtils.createFile(DATA_FILE_NAME, generateDocument.variables().toString().getBytes(StandardCharsets.UTF_8)),
                    null,
                    null,
                    null,
                    null,
                    null,
                    false,
                    FileUtils.createFile(MERGE_FILE_NAME, this.configuration.getMergeOptions()),
                    null,
                    null)
                    .onStatus(HttpStatusCode::is5xxServerError,
                            response -> response.bodyToMono(byte[].class)
                                    .flatMap(body -> Mono.error(new CosysException("Document could not be created."))))
                    .onStatus(HttpStatusCode::is4xxClientError,
                            response -> response.bodyToMono(byte[].class)
                                    .flatMap(body -> Mono.error(new CosysException("Document could not be created."))))
                    .bodyToMono(byte[].class);

        } catch (final IOException ex) {
            log.error("Document could not be created.", ex);
            throw new CosysException("Document could not be created.");
        }
    }

}
