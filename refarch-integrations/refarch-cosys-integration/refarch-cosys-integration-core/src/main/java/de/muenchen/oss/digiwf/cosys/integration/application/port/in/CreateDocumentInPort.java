package de.muenchen.oss.digiwf.cosys.integration.application.port.in;

import de.muenchen.oss.digiwf.cosys.integration.domain.exception.CosysException;
import de.muenchen.oss.digiwf.cosys.integration.domain.model.GenerateDocument;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public interface CreateDocumentInPort {

    void createDocument(
            @Valid final GenerateDocument generateDocument,
            @NotBlank final String filePath
    ) throws CosysException, DocumentStorageException;
}
