package de.muenchen.oss.refarch.integration.cosys.application.port.out;

import de.muenchen.oss.refarch.integration.cosys.domain.exception.DocumentGenerationException;
import de.muenchen.oss.refarch.integration.cosys.domain.model.GenerateDocument;
import jakarta.validation.Valid;
import java.io.InputStream;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

@Validated
public interface GenerateDocumentOutPort {
    /**
     * Generate a document based on the given input.
     *
     * @param generateDocument Parameters for generating the document.
     * @return The generated document.
     * @throws DocumentGenerationException If something goes wrong during document generation.
     */
    Mono<InputStream> generateCosysDocument(@Valid GenerateDocument generateDocument) throws DocumentGenerationException;
}
