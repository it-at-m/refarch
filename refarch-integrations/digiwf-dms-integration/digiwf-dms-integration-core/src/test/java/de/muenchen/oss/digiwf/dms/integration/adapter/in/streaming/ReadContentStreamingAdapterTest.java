package de.muenchen.oss.digiwf.dms.integration.adapter.in.streaming;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ReadContentStreamingAdapterTest extends StreamingAdapterTestBase {

    private final ReadContentDto readContentDto = new ReadContentDto(
            List.of("fileCoo"),
            "filepath",
            "filecontext",
            "user"
    );
    private Message<ReadContentDto> message;

    @BeforeEach
    void setup() {
        setupBase();
        Mockito.doNothing().when(readContentInPort).readContent(
                readContentDto.getContentCoos(),
                readContentDto.getUser(),
                readContentDto.getFilePath(),
                readContentDto.getFileContext(),
                processDefinitionId);


        this.message = new Message<>() {
            @Override
            public ReadContentDto getPayload() {
                return readContentDto;
            }

            @Override
            public MessageHeaders getHeaders() {
                return messageHeaders;
            }
        };
    }

    @Test
    void testDmsIntegrationReadContentSuccessfully() {
        streamingAdapter.readContent().accept(this.message);
        verify(readContentInPort, times(1)).readContent(
                readContentDto.getContentCoos(),
                readContentDto.getUser(),
                readContentDto.getFilePath(),
                readContentDto.getFileContext(),
                processDefinitionId);
    }

}

