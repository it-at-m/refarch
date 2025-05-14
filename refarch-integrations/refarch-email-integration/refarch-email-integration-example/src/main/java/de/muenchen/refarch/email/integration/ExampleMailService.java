package de.muenchen.refarch.email.integration;

import de.muenchen.refarch.email.integration.application.port.in.SendMailInPort;
import de.muenchen.refarch.email.integration.domain.model.Attachment;
import de.muenchen.refarch.email.integration.domain.model.TemplateMail;
import de.muenchen.refarch.email.integration.domain.model.TextMail;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExampleMailService {
    private final SendMailInPort sendMailInPort;

    public void testSendMail() {
        try (InputStream file = new ClassPathResource("/files/test-pdf.pdf").getInputStream()) {
            final Attachment attachment = new Attachment("test-pdf.pdf", new ByteArrayDataSource(file, "application/pdf"));
            final TextMail mail = new TextMail(
                    "test.receiver@muenchen.de",
                    null,
                    null,
                    "Test",
                    "This is a test",
                    null,
                    List.of(attachment));
            sendMailInPort.sendMailWithText(mail);
            log.info("Test mail sent");
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void testSendMailTemplate() {
        try (InputStream file = new ClassPathResource("/files/test-pdf.pdf").getInputStream()) {
            final Attachment attachment = new Attachment("test-pdf.pdf", new ByteArrayDataSource(file, "application/pdf"));
            final TemplateMail mail = new TemplateMail(
                    "test.receiver@muenchen.de",
                    null,
                    null,
                    "Test",
                    null,
                    List.of(attachment),
                    "test-template.ftl",
                    Map.of(
                            "subject", "Test",
                            "recipientName", "Test User"));
            sendMailInPort.sendMailWithTemplate(mail, "files/email-logo.png");
            log.info("Test template mail sent");
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
