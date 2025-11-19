package de.muenchen.refarch.integration.dms.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.refarch.integration.dms.application.port.out.ReadContentOutPort;
import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Content;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReadContentUseCaseTest {

    private final ReadContentOutPort readContent = mock(ReadContentOutPort.class);

    private final ReadContentUseCase readContentUseCase = new ReadContentUseCase(readContent);

    @Test
    void testReadContent() throws DmsException {

        final Content content = new Content("extension", "name", "content".getBytes(StandardCharsets.UTF_8));

        when(this.readContent.readContent(any(), any())).thenReturn(List.of(content));

        final List<Content> contentResponse = readContentUseCase.readContent(List.of("fileCoo"), "user");

        assertEquals(1, contentResponse.size());
        assertEquals(content, contentResponse.getFirst());
        verify(this.readContent, times(1)).readContent(List.of("fileCoo"), "user");
    }
}
