package de.muenchen.oss.refarch.integration.dms.application.port.out;

import de.muenchen.oss.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.oss.refarch.integration.dms.domain.model.Content;
import de.muenchen.oss.refarch.integration.dms.domain.model.DocumentType;
import de.muenchen.oss.refarch.integration.dms.domain.model.RequestContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.validation.annotation.Validated;

@Validated
public interface UpdateDocumentOutPort {

    void updateDocument(String documentCOO, DocumentType type, List<Content> contents, @NotNull @Valid RequestContext requestContext) throws DmsException;

}
