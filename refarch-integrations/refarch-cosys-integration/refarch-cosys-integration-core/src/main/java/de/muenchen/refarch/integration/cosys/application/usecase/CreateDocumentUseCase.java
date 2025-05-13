package de.muenchen.refarch.integration.cosys.application.usecase;

import de.muenchen.refarch.integration.cosys.application.port.in.CreateDocumentInPort;
import de.muenchen.refarch.integration.cosys.application.port.out.GenerateDocumentOutPort;
import de.muenchen.refarch.integration.cosys.application.port.out.SaveFileToStorageOutPort;
import de.muenchen.refarch.integration.cosys.domain.exception.CosysException;
import de.muenchen.refarch.integration.cosys.domain.model.GenerateDocument;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CreateDocumentUseCase implements CreateDocumentInPort {

    private final SaveFileToStorageOutPort saveFileToStorageOutPort;
    private final GenerateDocumentOutPort generateDocumentOutPort;

    /**
     * Generate a document in Cosys and save it in S3 using file context and path.
     *
     * @param generateDocument Data for generating documents
     * @param filePath Path to save document to
     */
    @Override
    public void createDocument(
            @Valid final GenerateDocument generateDocument,
            @NotBlank final String filePath) throws CosysException, DocumentStorageException {
        final byte[] data = this.generateDocumentOutPort.generateCosysDocument(generateDocument).block();
        this.saveFileToStorageOutPort.saveDocumentInStorage(filePath, data);
    }
}
