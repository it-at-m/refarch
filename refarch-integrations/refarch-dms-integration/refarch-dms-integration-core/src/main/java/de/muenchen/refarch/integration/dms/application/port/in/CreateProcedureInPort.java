package de.muenchen.refarch.integration.dms.application.port.in;

import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Procedure;
import jakarta.validation.constraints.NotBlank;

@SuppressWarnings("PMD.UseObjectForClearerAPI")
public interface CreateProcedureInPort {

    Procedure createProcedure(@NotBlank String titel, @NotBlank String fileCOO, String fileSubj, @NotBlank String user)
            throws DmsException;

}
