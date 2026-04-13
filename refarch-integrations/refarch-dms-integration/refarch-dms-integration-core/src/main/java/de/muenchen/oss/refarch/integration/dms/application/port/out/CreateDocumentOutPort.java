package de.muenchen.oss.refarch.integration.dms.application.port.out;

import de.muenchen.oss.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.oss.refarch.integration.dms.domain.model.Document;
import de.muenchen.oss.refarch.integration.dms.domain.model.RequestContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface CreateDocumentOutPort {

    String createDocument(Document document, @NotNull @Valid RequestContext requestContext) throws DmsException;

}
