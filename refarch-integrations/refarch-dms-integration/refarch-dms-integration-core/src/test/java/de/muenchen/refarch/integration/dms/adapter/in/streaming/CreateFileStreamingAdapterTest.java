package de.muenchen.refarch.integration.dms.adapter.in.streaming;

import de.muenchen.oss.digiwf.message.process.api.error.IncidentError;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.Map;

import static de.muenchen.oss.digiwf.message.common.MessageConstants.DIGIWF_PROCESS_INSTANCE_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CreateFileStreamingAdapterTest extends StreamingAdapterTestBase {

    private final CreateFileDto createFileDto = new CreateFileDto(
            "apentryCoo",
            "title",
            "user"
    );
    private Message<CreateFileDto> message;

    @BeforeEach
    void setup() {
        setupBase();
        Mockito.when(createFileInPortMock.createFile(
                        createFileDto.getTitle(),
                        createFileDto.getApentryCOO(),
                        createFileDto.getUser()))
                .thenReturn("coo");
        this.message = new Message<>() {
            @Override
            public CreateFileDto getPayload() {
                return createFileDto;
            }

            @Override
            public MessageHeaders getHeaders() {
                return messageHeaders;
            }
        };
    }

    @Test
    void testDmsIntegrationCreateFileSuccessfully() {
        streamingAdapter.createFile().accept(this.message);
        verify(createFileInPortMock, times(1)).createFile(createFileDto.getTitle(), createFileDto.getApentryCOO(), createFileDto.getUser());
    }

    @Test
    void testDmsIntegrationCreateFileHandlesValidationException() {
        Mockito.doThrow(new ValidationException("Test ValidationException")).when(createFileInPortMock).createFile(any(), any(), any());
        streamingAdapter.createFile().accept(this.message);
        final ArgumentCaptor<Map> messageHeaderArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(errorApiMock, times(1)).handleIncident(messageHeaderArgumentCaptor.capture(), any(IncidentError.class));
        Assertions.assertTrue(messageHeaderArgumentCaptor.getValue().containsKey(DIGIWF_PROCESS_INSTANCE_ID));
    }


    @Test
    void testDmsIntegrationCreateFileHandlesIncidentError() {
        Mockito.doThrow(new IncidentError("Error Message")).when(createFileInPortMock).createFile(any(), any(), any());
        streamingAdapter.createFile().accept(this.message);
        final ArgumentCaptor<Map> messageHeaderArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(errorApiMock, times(1)).handleIncident(messageHeaderArgumentCaptor.capture(), any(IncidentError.class));
        Assertions.assertTrue(messageHeaderArgumentCaptor.getValue().containsKey(DIGIWF_PROCESS_INSTANCE_ID));
    }
}

