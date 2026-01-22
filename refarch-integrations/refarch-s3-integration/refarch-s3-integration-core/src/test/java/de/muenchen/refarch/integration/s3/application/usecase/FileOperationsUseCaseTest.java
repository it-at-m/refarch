package de.muenchen.refarch.integration.s3.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.refarch.integration.s3.application.port.out.S3OutPort;
import de.muenchen.refarch.integration.s3.domain.exception.S3Exception;
import de.muenchen.refarch.integration.s3.domain.model.FileMetadata;
import de.muenchen.refarch.integration.s3.domain.model.FileReference;
import de.muenchen.refarch.integration.s3.domain.model.PresignedUrl;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FileOperationsUseCaseTest {

    @Mock
    private S3OutPort s3OutPort;

    private FileOperationsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FileOperationsUseCase(s3OutPort);
    }

    @Test
    void fileExists_delegatesToOutPort() throws S3Exception {
        FileReference ref = new FileReference("bucket", "key");
        when(s3OutPort.fileExists(ref)).thenReturn(true);
        assertThat(useCase.fileExists(ref)).isTrue();
        verify(s3OutPort).fileExists(ref);
    }

    @Test
    void saveFile_withKnownLength_delegates() throws S3Exception {
        FileReference ref = new FileReference("bucket", "key");
        byte[] bytes = "hello".getBytes();
        InputStream is = new ByteArrayInputStream(bytes);
        useCase.saveFile(ref, is, bytes.length);
        verify(s3OutPort).saveFile(ref, is, bytes.length);
    }

    @Test
    void saveFile_withUnknownLength_buffersAndDelegates() throws Exception {
        FileReference ref = new FileReference("bucket", "key");
        byte[] bytes = "hello-world".getBytes();
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);

        // Capture the stream and validate its content and the inferred length during the call
        doAnswer(invocation -> {
            InputStream captured = invocation.getArgument(1);
            long len = invocation.getArgument(2);
            byte[] read = captured.readAllBytes();
            assertEquals(bytes.length, len);
            assertThat(read).isEqualTo(bytes);
            return null;
        }).when(s3OutPort).saveFile(eq(ref), any(InputStream.class), anyLong());

        useCase.saveFile(ref, is);

        verify(s3OutPort, times(1)).saveFile(eq(ref), any(InputStream.class), eq((long) bytes.length));
    }

    @Test
    void saveFile_fromFile_delegates() throws Exception {
        FileReference ref = new FileReference("bucket", "key");
        File tmp = File.createTempFile("s3-usecase", ".bin");
        tmp.deleteOnExit();
        useCase.saveFile(ref, tmp);
        verify(s3OutPort).saveFile(ref, tmp);
    }

    @Test
    void getFileMetadata_delegatesAndReturns() throws S3Exception {
        FileReference ref = new FileReference("bucket", "key");
        FileMetadata metadata = new FileMetadata("key", 1L, "etag", Instant.now());
        when(s3OutPort.getFileMetadata(ref)).thenReturn(metadata);
        assertThat(useCase.getFileMetadata(ref)).isEqualTo(metadata);
        verify(s3OutPort).getFileMetadata(ref);
    }

    @Test
    void getPresignedUrl_delegatesWithDuration() throws S3Exception, MalformedURLException, URISyntaxException {
        FileReference ref = new FileReference("bucket", "key");
        PresignedUrl expected = new PresignedUrl(new URI("https://example.com").toURL(), "key", PresignedUrl.Action.GET);
        when(s3OutPort.getPresignedUrl(eq(ref), eq(PresignedUrl.Action.GET), any(Duration.class))).thenReturn(expected);
        PresignedUrl result = useCase.getPresignedUrl(ref, PresignedUrl.Action.GET, Duration.ofMinutes(15));
        assertThat(result).isEqualTo(expected);
        // capture duration to ensure minutes are as expected
        ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(s3OutPort).getPresignedUrl(eq(ref), eq(PresignedUrl.Action.GET), durationCaptor.capture());
        assertThat(durationCaptor.getValue()).isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    void getFileContent_delegatesAndReturns() throws S3Exception {
        FileReference ref = new FileReference("bucket", "key");
        InputStream is = new ByteArrayInputStream("x".getBytes());
        when(s3OutPort.getFileContent(ref)).thenReturn(is);
        assertThat(useCase.getFileContent(ref)).isEqualTo(is);
        verify(s3OutPort).getFileContent(ref);
    }

    @Test
    void deleteFile_delegates() throws S3Exception {
        FileReference ref = new FileReference("bucket", "key");
        useCase.deleteFile(ref);
        verify(s3OutPort).deleteFile(ref);
    }
}
