package de.muenchen.refarch.integration.dms.application.usecase;

import de.muenchen.refarch.integration.dms.application.port.out.CreateProcedureOutPort;
import de.muenchen.refarch.integration.dms.application.usecase.CreateProcedureUseCase;
import de.muenchen.refarch.integration.dms.domain.Procedure;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateProcedureUseCaseTest {

    private final CreateProcedureOutPort createProcedureOutPort = mock(CreateProcedureOutPort.class);

    private final CreateProcedureUseCase createProcedureUseCase = new CreateProcedureUseCase(createProcedureOutPort);

    @Test
    void createProcedure() {

        when(this.createProcedureOutPort.createProcedure(any(), any())).thenReturn(new Procedure("fileCOO", "title", "subject"));

        createProcedureUseCase.createProcedure("title", "fileCOO", "subject", "user");

        verify(this.createProcedureOutPort, times(1)).createProcedure(new Procedure("fileCOO", "title", "subject"), "user");
    }

}
