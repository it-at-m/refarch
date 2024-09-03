package de.muenchen.refarch.integration.dms.application.usecase;

import de.muenchen.refarch.integration.dms.application.port.out.DmsUserOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.ReadMetadataOutPort;
import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Metadata;
import de.muenchen.refarch.integration.dms.domain.model.ObjectType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.*;

class ReadMetadataUseCaseTest {

    private final ReadMetadataOutPort readMetadataOutPort = mock(ReadMetadataOutPort.class);
    private final DmsUserOutPort dmsUserOutPort = mock(DmsUserOutPort.class);

    private final ReadMetadataUseCase readMetadataUseCase = new ReadMetadataUseCase(readMetadataOutPort, dmsUserOutPort);

    @Test
    void readMetadata() throws DmsException {
        when(this.readMetadataOutPort.readMetadata(any(), any())).thenReturn(new Metadata("name", "Sachakte", "url"));
        when(this.dmsUserOutPort.getDmsUser()).thenReturn("user");

        readMetadataUseCase.readMetadata(ObjectType.Sachakte, "coo");

        verify(this.dmsUserOutPort, times(1)).getDmsUser();
        verify(this.readMetadataOutPort, times(1)).readMetadata("coo", "user");
    }

    @Test
    void readContentMetadata() throws DmsException {
        when(this.readMetadataOutPort.readContentMetadata(any(), any())).thenReturn(new Metadata("name", "type", "url"));
        when(this.dmsUserOutPort.getDmsUser()).thenReturn("user");

        readMetadataUseCase.readMetadata(ObjectType.Schriftstueck, "coo");

        verify(this.dmsUserOutPort, times(1)).getDmsUser();
        verify(this.readMetadataOutPort, times(1)).readContentMetadata("coo", "user");
    }

    @Test
    void readMetadataThrowsBpmnError() throws DmsException {
        when(this.readMetadataOutPort.readMetadata(any(), any())).thenReturn(new Metadata("name", "Ausgang", "url"));
        when(this.dmsUserOutPort.getDmsUser()).thenReturn("user");

        DmsException dmsException = catchThrowableOfType(() -> readMetadataUseCase.readMetadata(ObjectType.Sachakte, "coo"), DmsException.class);

        String expectedMessage = String.format("The input object with the COO address %s is invalid because it is of the object class %s and this does not match the expected object class(es) %s.", "coo", "Ausgang", "Sachakte");
        String actualMessage = dmsException.getMessage();

        assertThat(actualMessage).isEqualTo(expectedMessage);

        assertThat(dmsException.getStatusCode()).isEqualTo("WRONG_INPUT_OBJECT_CLASS");

        verify(this.dmsUserOutPort, times(1)).getDmsUser();
        verify(this.readMetadataOutPort, times(1)).readMetadata("coo", "user");
    }
}