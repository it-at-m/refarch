package de.muenchen.refarch.integration.dms.application.port.out;

import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.RequestContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public interface SearchFileOutPort {

    List<String> searchFile(@NotBlank String searchString, @Valid RequestContext requestContext, String reference, String value) throws DmsException;
}
