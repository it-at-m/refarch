package de.muenchen.oss.refarch.integration.dms.application.port.out;

import de.muenchen.oss.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.oss.refarch.integration.dms.domain.model.Metadata;
import de.muenchen.oss.refarch.integration.dms.domain.model.RequestContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface ReadMetadataOutPort {

    Metadata readMetadata(@NotBlank String coo, @NotNull @Valid RequestContext requestContext) throws DmsException;

    Metadata readContentMetadata(@NotBlank String coo, @NotNull @Valid RequestContext requestContext) throws DmsException;

}
