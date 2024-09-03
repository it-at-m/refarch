package de.muenchen.refarch.integration.dms.application.usecase;

import de.muenchen.refarch.integration.dms.application.port.out.ReadContentOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.TransferContentOutPort;
import de.muenchen.refarch.integration.dms.domain.model.Content;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReadContentUseCaseTest {

    private final TransferContentOutPort transferContentOutPort = mock(TransferContentOutPort.class);

    private final ReadContentOutPort readContent = mock(ReadContentOutPort.class);

    private final ReadContentUseCase readContentUseCase = new ReadContentUseCase(transferContentOutPort, readContent);

    @Test
    void readContent() {

        Content content = new Content("extension", "name", "content".getBytes());

        when(this.readContent.readContent(any(), any())).thenReturn(List.of(content));

        doNothing().when(transferContentOutPort).transferContent(any(), any());

        readContentUseCase.readContent(List.of("fileCoo"), "user", "filepath/");

        verify(this.readContent, times(1)).readContent(List.of("fileCoo"), "user");

        verify(this.transferContentOutPort, times(1)).transferContent(List.of(content), "filepath/");
    }
}