package de.muenchen.refarch.integration.dms.application.usecase;

import de.muenchen.refarch.integration.dms.application.port.out.DepositObjectOutPort;
import de.muenchen.refarch.integration.dms.application.usecase.DepositObjectUseCase;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DepositObjectUseCaseTest {

    private final DepositObjectOutPort depositObjectOutPort = mock(DepositObjectOutPort.class);

    private final DepositObjectUseCase depositObjectUseCase = new DepositObjectUseCase(depositObjectOutPort);

    @Test
    void depositObject() {

        doNothing().when(depositObjectOutPort).depositObject(any(), any());

        depositObjectUseCase.depositObject("objectCoo", "user");

        verify(this.depositObjectOutPort, times(1)).depositObject("objectCoo", "user");
    }


}
