package de.muenchen.refarch.integration.dms.application.port.in;

import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Metadata;
import de.muenchen.refarch.integration.dms.domain.model.ObjectType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public interface ReadMetadataInPort {

    Metadata readMetadata(@NotNull ObjectType objectclass, @NotBlank String coo, @NotBlank String user) throws DmsException;

}
