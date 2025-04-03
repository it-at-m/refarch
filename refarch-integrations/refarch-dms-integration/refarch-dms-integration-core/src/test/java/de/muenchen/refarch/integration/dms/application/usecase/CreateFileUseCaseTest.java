package de.muenchen.refarch.integration.dms.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.refarch.integration.dms.application.port.out.CreateFileOutPort;
import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.File;
import org.junit.jupiter.api.Test;

class CreateFileUseCaseTest {

    private final CreateFileOutPort createFileOutPort = mock(CreateFileOutPort.class);

    private final CreateFileUseCase createFileUseCase = new CreateFileUseCase(createFileOutPort);

    @Test
    void createFile() throws DmsException {

        when(this.createFileOutPort.createFile(any(), any())).thenReturn("fileCOO");

        createFileUseCase.createFile("title", "apentryCOO", "user");

        verify(this.createFileOutPort, times(1)).createFile(new File("apentryCOO", "title"), "user");

    }
}
