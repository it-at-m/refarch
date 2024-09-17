package de.muenchen.refarch.integration.dms.application.usecase;

import de.muenchen.refarch.integration.dms.application.port.out.CancelObjectOutPort;
import de.muenchen.refarch.integration.dms.application.usecase.CancelObjectUseCase;
import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CancelObjectUseCaseTest {

    private final CancelObjectOutPort cancelObjectOutPort = mock(CancelObjectOutPort.class);

    private final CancelObjectUseCase cancelObjectUseCase = new CancelObjectUseCase(cancelObjectOutPort);

    @Test
    void cancelObject() throws DmsException {

        doNothing().when(cancelObjectOutPort).cancelObject(any(), any());

        cancelObjectUseCase.cancelObject("objectCoo", "user");

        verify(this.cancelObjectOutPort, times(1)).cancelObject("objectCoo", "user");
    }

}
