package de.muenchen.refarch.integration.dms.application.usecase;

import de.muenchen.refarch.integration.dms.application.port.out.ReadMetadataOutPort;
import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Metadata;
import de.muenchen.refarch.integration.dms.domain.model.ObjectType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReadMetadataUseCaseTest {

    private final ReadMetadataOutPort readMetadataOutPort = mock(ReadMetadataOutPort.class);

    private final ReadMetadataUseCase readMetadataUseCase = new ReadMetadataUseCase(readMetadataOutPort);

    @Test
    void readMetadata() throws DmsException {
        when(this.readMetadataOutPort.readMetadata(any(), any())).thenReturn(new Metadata("name", "Sachakte", "url"));

        readMetadataUseCase.readMetadata(ObjectType.Sachakte, "coo", "user");

        verify(this.readMetadataOutPort, times(1)).readMetadata("coo", "user");
    }

    @Test
    void readContentMetadata() throws DmsException {
        when(this.readMetadataOutPort.readContentMetadata(any(), any())).thenReturn(new Metadata("name", "type", "url"));

        readMetadataUseCase.readMetadata(ObjectType.Schriftstueck, "coo", "user");

        verify(this.readMetadataOutPort, times(1)).readContentMetadata("coo", "user");
    }

    @Test
    void readMetadataThrowsDmsException() throws DmsException {
        when(this.readMetadataOutPort.readMetadata(any(), any())).thenReturn(new Metadata("name", "Ausgang", "url"));

        DmsException dmsException = catchThrowableOfType(() -> readMetadataUseCase.readMetadata(ObjectType.Sachakte, "coo", "user"), DmsException.class);

        String expectedMessage = String.format(
                "WRONG_INPUT_OBJECT_CLASS: The input object with the COO address %s is invalid because it is of the object class %s and this does not match the expected object class(es) %s.",
                "coo", "Ausgang", "Sachakte");
        String actualMessage = dmsException.getMessage();

        assertThat(actualMessage).isEqualTo(expectedMessage);

        assertThat(dmsException.getStatusCode()).isEqualTo("WRONG_INPUT_OBJECT_CLASS");

        verify(this.readMetadataOutPort, times(1)).readMetadata("coo", "user");
    }
}
