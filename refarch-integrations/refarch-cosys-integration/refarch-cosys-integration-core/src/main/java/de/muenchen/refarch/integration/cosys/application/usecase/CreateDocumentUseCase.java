package de.muenchen.refarch.integration.cosys.application.usecase;

import de.muenchen.refarch.integration.cosys.application.port.in.CreateDocumentInPort;
import de.muenchen.refarch.integration.cosys.application.port.out.GenerateDocumentOutPort;
import de.muenchen.refarch.integration.cosys.domain.exception.CosysException;
import de.muenchen.refarch.integration.cosys.domain.model.GenerateDocument;
import jakarta.validation.Valid;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CreateDocumentUseCase implements CreateDocumentInPort {
    private final GenerateDocumentOutPort generateDocumentOutPort;

    /**
     * Generate a document in Cosys and save it in S3 using file context and path.
     *
     * @param generateDocument Data for generating documents
     */
    @Override
    public InputStream createDocument(@Valid final GenerateDocument generateDocument) throws CosysException {
        return this.generateDocumentOutPort.generateCosysDocument(generateDocument).block();
    }
}
