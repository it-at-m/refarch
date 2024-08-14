package de.muenchen.refarch.email.integration.application.usecase;

import de.muenchen.refarch.email.integration.application.port.in.SendMailPathsInPort;
import de.muenchen.refarch.email.integration.application.port.out.LoadMailAttachmentOutPort;
import de.muenchen.refarch.email.integration.application.port.out.MailOutPort;
import de.muenchen.refarch.email.integration.domain.exception.TemplateError;
import de.muenchen.refarch.email.integration.domain.model.paths.BasicMailPaths;
import de.muenchen.refarch.email.integration.domain.model.paths.TemplateMailPaths;
import de.muenchen.refarch.email.integration.domain.model.paths.TextMailPaths;
import de.muenchen.refarch.email.model.FileAttachment;
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
public class SendMailPathsUseCase implements SendMailPathsInPort {

    private final LoadMailAttachmentOutPort loadAttachmentOutPort;
    private final MailOutPort mailOutPort;

    /**
     * Send a mail.
     *
     * @param mail mail that is sent
     */
    @Override
    public void sendMailWithText(@Valid final TextMailPaths mail) {
        de.muenchen.refarch.email.model.Mail mailModel = createMail(mail);
        mailModel.setBody(mail.getBody());

        this.sendMail(mailModel, null);
    }

    @Override
    public void sendMailWithTemplate(@Valid final TemplateMailPaths mail) throws TemplateError {
        // get body from template
        try {
            Map<String, Object> content = new HashMap<>(mail.getContent());
            content.put("footer", "DigiWF 2.0<br>IT-Referat der Stadt München");
            String body = this.mailOutPort.getBodyFromTemplate(mail.getTemplate(), content);

            de.muenchen.refarch.email.model.Mail mailModel = createMail(mail);
            mailModel.setBody(body);
            mailModel.setHtmlBody(true);

            this.sendMail(mailModel, "templates/email-logo.png");

        } catch (IOException ioException) {
            throw new TemplateError("The template " + mail.getTemplate() + " could not be loaded");
        } catch (TemplateException templateException) {
            throw new TemplateError(templateException.getMessage());
        }
    }

    private de.muenchen.refarch.email.model.Mail createMail(BasicMailPaths mail) {
        // load Attachments
        List<FileAttachment> attachments = loadAttachmentOutPort.loadAttachments(mail.getFilePaths());

        // send mail
        return de.muenchen.refarch.email.model.Mail.builder()
                .receivers(mail.getReceivers())
                .subject(mail.getSubject())
                .replyTo(mail.getReplyTo())
                .receiversCc(mail.getReceiversCc())
                .receiversBcc(mail.getReceiversBcc())
                .attachments(attachments)
                .build();
    }

    private void sendMail(de.muenchen.refarch.email.model.Mail mailModel, String logoPath) throws MailSendException {
        try {
            this.mailOutPort.sendMail(mailModel, logoPath);
        } catch (final MessagingException ex) {
            log.error("Sending mail failed with exception: {}", ex.getMessage());
            throw new MailSendException(ex.getMessage());
        }
    }

}
