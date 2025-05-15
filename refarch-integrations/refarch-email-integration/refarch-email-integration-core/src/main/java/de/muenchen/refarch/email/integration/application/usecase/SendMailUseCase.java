package de.muenchen.refarch.email.integration.application.usecase;

import de.muenchen.refarch.email.integration.application.port.in.SendMailInPort;
import de.muenchen.refarch.email.integration.application.port.out.MailOutPort;
import de.muenchen.refarch.email.integration.domain.exception.TemplateError;
import de.muenchen.refarch.email.integration.domain.model.BasicMail;
import de.muenchen.refarch.email.integration.domain.model.Mail;
import de.muenchen.refarch.email.integration.domain.model.TemplateMail;
import de.muenchen.refarch.email.integration.domain.model.TextMail;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSendException;
import org.springframework.validation.annotation.Validated;

@Slf4j
@RequiredArgsConstructor
@Validated
public class SendMailUseCase implements SendMailInPort {
    private final MailOutPort mailOutPort;

    @Override
    public void sendMailWithText(@Valid final TextMail mail) {
        this.sendMailWithText(mail, null);
    }

    @Override
    public void sendMailWithText(@Valid final TextMail mail, final String logoPath) {
        final Mail mailModel = createMail(mail, mail.getBody(), false);
        this.sendMail(mailModel, logoPath);
    }

    @Override
    public void sendMailWithTemplate(@Valid final TemplateMail mail) {
        this.sendMailWithTemplate(mail, null);
    }

    @Override
    public void sendMailWithTemplate(@Valid final TemplateMail mail, final String logoPath) {
        // get body from template
        try {
            final Map<String, Object> content = new HashMap<>(mail.getContent());
            final String body = this.mailOutPort.getBodyFromTemplate(mail.getTemplate(), content);

            final Mail mailModel = createMail(mail, body, true);

            this.sendMail(mailModel, logoPath);

        } catch (IOException ioException) {
            throw new TemplateError("The template " + mail.getTemplate() + " could not be loaded", ioException);
        } catch (TemplateException templateException) {
            throw new TemplateError(templateException.getMessage(), templateException);
        }
    }

    private Mail createMail(final BasicMail mail, final String body, final boolean htmlBody) {
        // send mail
        return new Mail(
                mail.getReceivers(),
                mail.getReceiversCc(),
                mail.getReceiversBcc(),
                mail.getSubject(),
                body,
                htmlBody,
                null,
                mail.getReplyTo(),
                mail.getAttachments());
    }

    private void sendMail(final Mail mailModel, final String logoPath) {
        try {
            this.mailOutPort.sendMail(mailModel, logoPath);
        } catch (final MessagingException ex) {
            log.error("Sending mail failed with exception: {}", ex.getMessage());
            throw new MailSendException(ex.getMessage(), ex);
        }
    }

}
