package de.muenchen.refarch.integration.dms.application.port.out;

import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.File;
import de.muenchen.refarch.integration.dms.domain.model.RequestContext;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@Validated
public interface CreateFileOutPort {

    String createFile(@Valid File file, @Valid RequestContext requestContext) throws DmsException;

}
