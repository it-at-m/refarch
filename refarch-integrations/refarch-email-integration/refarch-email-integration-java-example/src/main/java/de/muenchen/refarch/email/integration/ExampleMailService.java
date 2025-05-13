package de.muenchen.refarch.email.integration;

import de.muenchen.refarch.email.integration.application.port.in.SendMailInPort;
import de.muenchen.refarch.email.integration.domain.model.TextMail;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFileRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExampleMailService {
    private final SendMailInPort sendMailInPort;
    private final DocumentStorageFileRepository documentStorageFileRepository;

    public void testSendMail() {
        this.uploadTestFile();
        final TextMail mail = new TextMail(
                "test.receiver@muenchen.de",
                null,
                null,
                "Test",
                "This is a test",
                null,
                List.of("/test/test-pdf.pdf"));
        sendMailInPort.sendMailWithText(mail);
        log.info("Test mail sent");
    }

    @SneakyThrows
    private void uploadTestFile() {
        final ClassPathResource resource = new ClassPathResource("/files/test-pdf.pdf");
        documentStorageFileRepository.updateFile("/test/test-pdf.pdf", resource.getContentAsByteArray(), 1);
        log.info("Test file uploaded");
    }
}
