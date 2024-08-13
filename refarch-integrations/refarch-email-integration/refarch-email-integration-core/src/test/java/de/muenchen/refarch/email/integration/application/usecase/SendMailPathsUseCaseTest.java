package de.muenchen.refarch.email.integration.application.usecase;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.muenchen.refarch.email.integration.application.port.in.SendMailPathsInPort;
import de.muenchen.refarch.email.integration.application.port.out.LoadMailAttachmentOutPort;
import de.muenchen.refarch.email.integration.application.port.out.MailOutPort;
import de.muenchen.refarch.email.integration.domain.exception.TemplateError;
import de.muenchen.refarch.email.integration.domain.model.paths.TemplateMailPaths;
import de.muenchen.refarch.email.integration.domain.model.paths.TextMailPaths;
import de.muenchen.refarch.email.model.FileAttachment;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;

class SendMailPathsUseCaseTest {

    private final LoadMailAttachmentOutPort loadMailAttachmentOutPort = mock(LoadMailAttachmentOutPort.class);
    private final MailOutPort mailOutPort = mock(MailOutPort.class);
    private final TextMailPaths mail = new TextMailPaths(
            "mailReceiver1@muenchen.de,mailReceiver2@muenchen.de",
            "receiverCC@muenchen.de",
            "receiverBCC@muenchen.de",
            "Test Mail",
            "This is a test mail",
            "test@muenchen.de",
            "fileContext",
            "folder/file.txt");
    private final TemplateMailPaths templateMail = new TemplateMailPaths(
            "mailReceiver1@muenchen.de,mailReceiver2@muenchen.de",
            "receiverCC@muenchen.de",
            "receiverBCC@muenchen.de",
            "Test Mail",
            "test@muenchen.de",
            "fileContext",
            "folder/file.txt",
            "template",
            Map.of("mail", Map.of()));
    private SendMailPathsInPort sendMailPathsInPort;

    @BeforeEach
    void setUp() {
        this.sendMailPathsInPort = new SendMailPathsUseCase(loadMailAttachmentOutPort, mailOutPort);
    }

    @Test
    void sendMail() throws MessagingException {
        sendMailPathsInPort.sendMailWithText(mail);
        final de.muenchen.refarch.email.model.Mail mailOutModel = de.muenchen.refarch.email.model.Mail.builder()
                .receivers(mail.getReceivers())
                .subject(mail.getSubject())
                .body(mail.getBody())
                .replyTo(mail.getReplyTo())
                .receiversCc(mail.getReceiversCc())
                .receiversBcc(mail.getReceiversBcc())
                .attachments(List.of())
                .build();
        verify(mailOutPort).sendMail(mailOutModel, null);
    }

    @Test
    void sendMailWithAttachments() throws MessagingException {
        final FileAttachment fileAttachment = new FileAttachment("test.txt", new ByteArrayDataSource("Anhang Inhalt".getBytes(), "text/plain"));
        when(loadMailAttachmentOutPort.loadAttachments(List.of("folder/file.txt"))).thenReturn(List.of(fileAttachment));

        sendMailPathsInPort.sendMailWithText(mail);
        final de.muenchen.refarch.email.model.Mail mailOutModel = de.muenchen.refarch.email.model.Mail.builder()
                .receivers(mail.getReceivers())
                .subject(mail.getSubject())
                .body(mail.getBody())
                .replyTo(mail.getReplyTo())
                .receiversCc(mail.getReceiversCc())
                .receiversBcc(mail.getReceiversBcc())
                .attachments(List.of(fileAttachment))
                .build();
        verify(mailOutPort).sendMail(mailOutModel, null);
    }

    @Test
    void sendMailThrowsMailSendException() throws MessagingException {
        doThrow(new MessagingException("Test Exception")).when(mailOutPort).sendMail(any(), any());
        assertThatThrownBy(() -> sendMailPathsInPort.sendMailWithText(mail)).isInstanceOf(MailSendException.class);
    }

    @Test
    void sendMailWithTemplate() throws MessagingException, TemplateException, IOException {
        when(mailOutPort.getBodyFromTemplate(anyString(), anyMap())).thenReturn("generated body");
        sendMailPathsInPort.sendMailWithTemplate(templateMail);
        final de.muenchen.refarch.email.model.Mail mailOutModel = de.muenchen.refarch.email.model.Mail.builder()
                .receivers(mail.getReceivers())
                .subject(mail.getSubject())
                .htmlBody(true)
                .body("generated body")
                .replyTo(mail.getReplyTo())
                .receiversCc(mail.getReceiversCc())
                .receiversBcc(mail.getReceiversBcc())
                .attachments(List.of())
                .build();
        verify(mailOutPort).sendMail(mailOutModel, "templates/email-logo.png");
    }

    @Test
    void sendMailWithTemplateThrowsIOException() throws TemplateException, IOException {
        doThrow(new IOException("IO Exception")).when(mailOutPort).getBodyFromTemplate(anyString(), anyMap());
        TemplateError error = catchThrowableOfType(() -> sendMailPathsInPort.sendMailWithTemplate(templateMail), TemplateError.class);

        String expectedMessage = "The template " + templateMail.getTemplate() + " could not be loaded";
        String actualMessage = error.getMessage();

        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @Test
    void sendMailWithTemplateThrowsTemplateException() throws TemplateException, IOException {
        TemplateException templateException = mock(TemplateException.class);
        when(templateException.getMessage()).thenReturn("Template Exception Message");
        doThrow(templateException).when(mailOutPort).getBodyFromTemplate(anyString(), anyMap());
        TemplateError error = catchThrowableOfType(() -> sendMailPathsInPort.sendMailWithTemplate(templateMail), TemplateError.class);

        String expectedMessage = "Template Exception Message";
        String actualMessage = error.getMessage();

        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

}
