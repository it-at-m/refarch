package de.muenchen.refarch.integration.dms.application.port.out;

import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.RequestContext;
import jakarta.validation.Valid;
import java.util.List;

public interface SearchSubjectAreaOutPort {

    List<String> searchSubjectArea(String searchString, @Valid RequestContext requestContext) throws DmsException;
}
