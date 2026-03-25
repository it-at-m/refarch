package de.muenchen.refarch.integration.dms.application.port.out;

import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Metadata;
import de.muenchen.refarch.integration.dms.domain.model.RequestContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

@Validated
public interface ReadMetadataOutPort {

    Metadata readMetadata(@NotBlank String coo, @Valid RequestContext requestContext) throws DmsException;

    Metadata readContentMetadata(@NotBlank String coo, @Valid RequestContext requestContext) throws DmsException;

}
