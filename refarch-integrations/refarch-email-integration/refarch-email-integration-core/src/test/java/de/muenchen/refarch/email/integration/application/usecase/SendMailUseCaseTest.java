package de.muenchen.refarch.email.integration.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.refarch.email.integration.application.port.in.SendMailInPort;
import de.muenchen.refarch.email.integration.application.port.out.MailOutPort;
import de.muenchen.refarch.email.integration.domain.exception.TemplateError;
import de.muenchen.refarch.email.integration.domain.model.Attachment;
import de.muenchen.refarch.email.integration.domain.model.Mail;
import de.muenchen.refarch.email.integration.domain.model.TemplateMail;
import de.muenchen.refarch.email.integration.domain.model.TextMail;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;

class SendMailUseCaseTest {

    private final MailOutPort mailOutPort = mock(MailOutPort.class);
    private final Attachment attachment = new Attachment("file.pdf", new ByteArrayDataSource("Content".getBytes(), "application/pdf"));
    private final TextMail mail = new TextMail(
            "mailReceiver1@muenchen.de,mailReceiver2@muenchen.de",
            "receiverCC@muenchen.de",
            "receiverBCC@muenchen.de",
            "Test Mail",
            "This is a test mail",
            "test@muenchen.de",
            List.of());
    private final TextMail mailWithAttachment = new TextMail(
            "mailReceiver1@muenchen.de,mailReceiver2@muenchen.de",
            "receiverCC@muenchen.de",
            "receiverBCC@muenchen.de",
            "Test Mail",
            "This is a test mail",
            "test@muenchen.de",
            List.of(attachment));
    private final TemplateMail templateMail = new TemplateMail(
            "mailReceiver1@muenchen.de,mailReceiver2@muenchen.de",
            "receiverCC@muenchen.de",
            "receiverBCC@muenchen.de",
            "Test Mail",
            "test@muenchen.de",
            List.of(),
            "template",
            Map.of("mail", Map.of()));
    private SendMailInPort sendMailInPort;

    @BeforeEach
    void setUp() {
        this.sendMailInPort = new SendMailUseCase(mailOutPort);
    }

    @Test
    void sendMail() throws MessagingException {
        sendMailInPort.sendMailWithText(mail);
        final Mail mailOutModel = new Mail(
                mail.getReceivers(),
                mail.getReceiversCc(),
                mail.getReceiversBcc(),
                mail.getSubject(),
                mail.getBody(),
                false,
                null,
                mail.getReplyTo(),
                List.of());
        verify(mailOutPort).sendMail(mailOutModel, null);
    }

    @Test
    void sendMailWithAttachments() throws MessagingException {
        sendMailInPort.sendMailWithText(mailWithAttachment);
        final Mail mailOutModel = new Mail(
                mail.getReceivers(),
                mail.getReceiversCc(),
                mail.getReceiversBcc(),
                mail.getSubject(),
                mail.getBody(),
                false,
                null,
                mail.getReplyTo(),
                List.of(attachment));
        verify(mailOutPort).sendMail(mailOutModel, null);
    }

    @Test
    void sendMailThrowsMailSendException() throws MessagingException {
        doThrow(new MessagingException("Test Exception")).when(mailOutPort).sendMail(any(), any());
        assertThatThrownBy(() -> sendMailInPort.sendMailWithText(mail)).isInstanceOf(MailSendException.class);
    }

    @Test
    void sendMailWithTemplate() throws MessagingException, TemplateException, IOException {
        when(mailOutPort.getBodyFromTemplate(anyString(), anyMap())).thenReturn("generated body");
        sendMailInPort.sendMailWithTemplate(templateMail, "templates/email-logo.png");
        final Mail mailOutModel = new Mail(
                mail.getReceivers(),
                mail.getReceiversCc(),
                mail.getReceiversBcc(),
                mail.getSubject(),
                "generated body",
                true,
                null,
                mail.getReplyTo(),
                List.of());
        verify(mailOutPort).sendMail(mailOutModel, "templates/email-logo.png");
    }

    @Test
    void sendMailWithTemplateThrowsIOException() throws TemplateException, IOException {
        doThrow(new IOException("IO Exception")).when(mailOutPort).getBodyFromTemplate(anyString(), anyMap());
        final TemplateError error = catchThrowableOfType(TemplateError.class, () -> sendMailInPort.sendMailWithTemplate(templateMail));

        final String expectedMessage = "The template " + templateMail.getTemplate() + " could not be loaded";
        final String actualMessage = error.getMessage();

        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @Test
    void sendMailWithTemplateThrowsTemplateException() throws TemplateException, IOException {
        final TemplateException templateException = mock(TemplateException.class);
        when(templateException.getMessage()).thenReturn("Template Exception Message");
        doThrow(templateException).when(mailOutPort).getBodyFromTemplate(anyString(), anyMap());
        final TemplateError error = catchThrowableOfType(TemplateError.class, () -> sendMailInPort.sendMailWithTemplate(templateMail));

        final String expectedMessage = "Template Exception Message";
        final String actualMessage = error.getMessage();

        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

}
