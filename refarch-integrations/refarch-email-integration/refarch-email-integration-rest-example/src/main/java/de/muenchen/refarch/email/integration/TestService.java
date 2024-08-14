package de.muenchen.refarch.email.integration;

import de.muenchen.refarch.email.integration.application.port.in.SendMailPathsInPort;
import de.muenchen.refarch.email.integration.domain.model.paths.TextMailPaths;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestService {
    private final SendMailPathsInPort sendMailPathsInPort;
    private final DocumentStorageFileRepository documentStorageFileRepository;

    void testSendMail() {
        this.uploadTestFile();
        TextMailPaths mail = new TextMailPaths();
        mail.setReceivers("test.receiver@muenchen.de");
        mail.setSubject("Test");
        mail.setBody("This is a test");
        mail.setFilePaths("/test/test-pdf.pdf");
        sendMailPathsInPort.sendMailWithText(mail);
        log.info("Test mail sent");
    }

    @SneakyThrows
    void uploadTestFile() {
        ClassPathResource resource = new ClassPathResource("/files/test-pdf.pdf");
        documentStorageFileRepository.updateFile("/test/test-pdf.pdf", resource.getContentAsByteArray(), 1);
        log.info("Test file uploaded");
    }
}
