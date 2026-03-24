package de.muenchen.refarch.email.integration.adapter.out.mail;

import de.muenchen.refarch.email.integration.application.port.out.MailOutPort;
import de.muenchen.refarch.email.integration.domain.exception.SendMailException;
import de.muenchen.refarch.email.integration.domain.exception.TemplateException;
import de.muenchen.refarch.email.integration.domain.model.Attachment;
import de.muenchen.refarch.email.integration.domain.model.Mail;
import de.muenchen.refarch.email.integration.domain.model.TemplateMail;
import de.muenchen.refarch.email.integration.domain.model.TextMail;
import freemarker.template.Template;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

@RequiredArgsConstructor
@Slf4j
@Validated
public class MailAdapter implements MailOutPort {
    private final JavaMailSender mailSender;
    private final ResourceLoader resourceLoader;
    private final FreeMarkerConfigurer freeMarkerConfigurer;
    private final String fromAddress;
    private final String defaultReplyToAddress;

    @Override
    public void sendTextMail(@Valid final TextMail mail) {
        final Mail mailModel = new Mail(mail, mail.getBody(), false);
        this.sendMail(mailModel, null);
    }

    @Override
    public void sendHtmlMailWithTemplate(@Valid final TemplateMail mail) {
        this.sendHtmlMailWithTemplate(mail, null);
    }

    @Override
    public void sendHtmlMailWithTemplate(@Valid final TemplateMail mail, final String logoPath) {
        final Map<String, Object> content = new HashMap<>(mail.getContent());
        final String body = this.getBodyFromTemplate(mail.getTemplate(), content);

        final Mail mailModel = new Mail(mail, body, true);
        this.sendMail(mailModel, logoPath);
    }

    @Override
    public void sendMail(final Mail mail) {
        this.sendMail(mail, null);
    }

    @Override
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public void sendMail(final Mail mail, final String logoPath) {
        try {
            final MimeMessage mimeMessage = constructMimeMessage(mail);
            final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            helper.setSubject(mail.subject());
            helper.setText(mail.body(), mail.htmlBody());

            // use custom sender
            helper.setFrom(mail.hasSender() ? mail.sender() : this.fromAddress);

            // mail attachments
            if (mail.hasAttachment()) {
                for (final Attachment attachment : mail.attachments()) {
                    helper.addAttachment(attachment.fileName(), attachment.file());
                }
            }

            // logo
            if (logoPath != null) {
                final Resource logo = this.getRessourceFromClassPath(logoPath);
                if (logo.exists()) {
                    helper.addInline("logo", logo);
                } else {
                    log.warn("Logo resource '{}' not found – sending mail without logo", logoPath);
                }
            }

            this.mailSender.send(mimeMessage);
            log.info("Mail with subject '{}' sent to '{}'", mail.subject(), mail.receivers());
        } catch (final MessagingException e) {
            throw new SendMailException("Error while constructing mail to send", e);
        } catch (final MailException e) {
            throw new SendMailException("Error while sending mail", e);
        }
    }

    /**
     * Renders template with given properties.
     *
     * @param templateName The template to render.
     * @param content The value to insert into template.
     * @return The rendered template as String.
     * @throws TemplateException If {@link freemarker.template.TemplateException} or {@link IOException}
     *             is thrown during rendering.
     */
    protected String getBodyFromTemplate(final String templateName, final Map<String, Object> content) {
        try {
            final Template template = freeMarkerConfigurer.getConfiguration().getTemplate(templateName);
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, content);
        } catch (final IOException | freemarker.template.TemplateException e) {
            throw new TemplateException("Error while rendering template", e);
        }
    }

    /**
     * Construct MimeMessage from given domain Mail class.
     *
     * @param mail The mail to use for construction.
     * @return The constructed MimeMessage.
     * @throws MessagingException If one of the mail addresses can't be parsed.
     */
    private MimeMessage constructMimeMessage(final Mail mail) throws MessagingException {
        final MimeMessage mimeMessage = this.mailSender.createMimeMessage();

        mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail.receivers()));

        if (mail.hasReceiversCc()) {
            mimeMessage.setRecipients(Message.RecipientType.CC, InternetAddress.parse(mail.receiversCc()));
        }
        if (mail.hasReceiversBcc()) {
            mimeMessage.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(mail.receiversBcc()));
        }

        if (mail.hasReplyTo()) {
            mimeMessage.setReplyTo(InternetAddress.parse(mail.replyTo()));
        } else if (defaultReplyToAddress != null) {
            mimeMessage.setReplyTo(InternetAddress.parse(defaultReplyToAddress));
        }
        return mimeMessage;
    }

    /**
     * Load file from classpath as resource.
     *
     * @param path The path to file to load (without 'classpath:').
     * @return The file as resource.
     */
    private Resource getRessourceFromClassPath(final String path) {
        return resourceLoader.getResource("classpath:" + path);
    }
}
