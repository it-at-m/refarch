package de.muenchen.refarch.email.integration.application.usecase;

import de.muenchen.refarch.email.integration.application.port.in.SendMailInPort;
import de.muenchen.refarch.email.integration.application.port.out.LoadMailAttachmentOutPort;
import de.muenchen.refarch.email.integration.application.port.out.MailOutPort;
import de.muenchen.refarch.email.integration.domain.exception.TemplateError;
import de.muenchen.refarch.email.integration.domain.model.BasicMail;
import de.muenchen.refarch.email.integration.domain.model.TemplateMail;
import de.muenchen.refarch.email.integration.domain.model.TextMail;
import de.muenchen.refarch.email.model.FileAttachment;
import de.muenchen.refarch.email.model.Mail;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSendException;
import org.springframework.validation.annotation.Validated;

@Slf4j
@RequiredArgsConstructor
@Validated
public class SendMailUseCase implements SendMailInPort {

    private final LoadMailAttachmentOutPort loadAttachmentOutPort;
    private final MailOutPort mailOutPort;

    /**
     * Send a mail.
     *
     * @param mail mail that is sent
     */
    @Override
    public void sendMailWithText(@Valid final TextMail mail) {
        Mail mailModel = createMail(mail, mail.getBody(), false);

        this.sendMail(mailModel, null);
    }

    @Override
    public void sendMailWithTemplate(@Valid final TemplateMail mail) throws TemplateError {
        // get body from template
        try {
            Map<String, Object> content = new HashMap<>(mail.getContent());
            String body = this.mailOutPort.getBodyFromTemplate(mail.getTemplate(), content);

            Mail mailModel = createMail(mail, body, true);

            this.sendMail(mailModel, "templates/email-logo.png");

        } catch (IOException ioException) {
            throw new TemplateError("The template " + mail.getTemplate() + " could not be loaded");
        } catch (TemplateException templateException) {
            throw new TemplateError(templateException.getMessage());
        }
    }

    private Mail createMail(final BasicMail mail, final String body, final boolean htmlBody) {
        // load Attachments
        List<FileAttachment> attachments = loadAttachmentOutPort.loadAttachments(mail.getFilePaths());

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
                attachments
        );
    }

    private void sendMail(Mail mailModel, String logoPath) throws MailSendException {
        try {
            this.mailOutPort.sendMail(mailModel, logoPath);
        } catch (final MessagingException ex) {
            log.error("Sending mail failed with exception: {}", ex.getMessage());
            throw new MailSendException(ex.getMessage());
        }
    }

}
