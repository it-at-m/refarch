package de.muenchen.refarch.integration.dms.application.port.in;

import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import jakarta.validation.constraints.NotBlank;

public interface DepositObjectInPort {

    void depositObject(@NotBlank final String objectCoo, @NotBlank final String user) throws DmsException;

}
