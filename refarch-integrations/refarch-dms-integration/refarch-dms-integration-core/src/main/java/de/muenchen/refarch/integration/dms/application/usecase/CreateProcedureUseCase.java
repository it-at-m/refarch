package de.muenchen.refarch.integration.dms.application.usecase;

import de.muenchen.refarch.integration.dms.application.port.out.CreateProcedureOutPort;
import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Procedure;
import de.muenchen.refarch.integration.dms.application.port.in.CreateProcedureInPort;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
@RequiredArgsConstructor
@SuppressWarnings("PMD.UseObjectForClearerAPI")
public class CreateProcedureUseCase implements CreateProcedureInPort {

    private final CreateProcedureOutPort createProcedureOutPort;

    @Override
    public Procedure createProcedure(
            @NotBlank final String titel,
            @NotBlank final String fileCOO,
            final String fileSubj,
            @NotBlank final String user) throws DmsException {
        final Procedure procedure = new Procedure(fileCOO, titel, fileSubj);

        return createProcedureOutPort.createProcedure(procedure, user);
    }
}
