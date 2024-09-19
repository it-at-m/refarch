package de.muenchen.refarch.integration.dms.application.port.in;

import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Procedure;
import jakarta.validation.constraints.NotBlank;

public interface CreateProcedureInPort {

    Procedure createProcedure(@NotBlank final String titel, @NotBlank final String fileCOO, final String fileSubj, @NotBlank final String user)
            throws DmsException;

}
