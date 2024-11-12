package de.muenchen.refarch.integration.dms.application.usecase;

import de.muenchen.refarch.integration.dms.application.port.out.CreateProcedureOutPort;
import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Procedure;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateProcedureUseCaseTest {

    private final CreateProcedureOutPort createProcedureOutPort = mock(CreateProcedureOutPort.class);

    private final CreateProcedureUseCase createProcedureUseCase = new CreateProcedureUseCase(createProcedureOutPort);

    @Test
    void createProcedure() throws DmsException {

        when(this.createProcedureOutPort.createProcedure(any(), any())).thenReturn(new Procedure("fileCOO", "title", "subject"));

        createProcedureUseCase.createProcedure("title", "fileCOO", "subject", "user");

        verify(this.createProcedureOutPort, times(1)).createProcedure(new Procedure("fileCOO", "title", "subject"), "user");
    }

}
