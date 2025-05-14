package de.muenchen.refarch.integration.cosys.application.port.in;

import de.muenchen.refarch.integration.cosys.domain.exception.CosysException;
import de.muenchen.refarch.integration.cosys.domain.model.GenerateDocument;
import jakarta.validation.Valid;
import java.io.InputStream;
import org.springframework.validation.annotation.Validated;

@Validated
public interface CreateDocumentInPort {

    InputStream createDocument(@Valid GenerateDocument generateDocument) throws CosysException;
}
