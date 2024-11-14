package de.muenchen.refarch.integration.cosys.application.port.out;

import de.muenchen.refarch.integration.cosys.domain.exception.CosysException;
import de.muenchen.refarch.integration.cosys.domain.model.GenerateDocument;
import reactor.core.publisher.Mono;

public interface GenerateDocumentOutPort {

    Mono<byte[]> generateCosysDocument(GenerateDocument generateDocument) throws CosysException;

}
