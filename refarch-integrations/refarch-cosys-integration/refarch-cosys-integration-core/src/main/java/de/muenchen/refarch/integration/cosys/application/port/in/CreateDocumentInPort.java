package de.muenchen.refarch.integration.cosys.application.port.in;

import de.muenchen.refarch.integration.cosys.domain.exception.CosysException;
import de.muenchen.refarch.integration.cosys.domain.model.GenerateDocument;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public interface CreateDocumentInPort {

    void createDocument(
            @Valid GenerateDocument generateDocument,
            @NotBlank String filePath) throws CosysException, DocumentStorageException;
}
