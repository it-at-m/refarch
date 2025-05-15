package de.muenchen.refarch.email.integration.adapter.out.mail;

import de.muenchen.refarch.email.integration.application.port.out.MailOutPort;
import de.muenchen.refarch.email.integration.domain.model.Attachment;
import de.muenchen.refarch.email.integration.domain.model.Mail;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

@RequiredArgsConstructor
@Slf4j
public class MailAdapter implements MailOutPort {
    private final JavaMailSender mailSender;
    private final ResourceLoader resourceLoader;
    private final FreeMarkerConfigurer freeMarkerConfigurer;
    private final String fromAddress;
    private final String defaultReplyToAddress;

    @Override
    public void sendMail(final Mail mail) throws MessagingException {
        this.sendMail(mail, null);
    }

    @Override
    public void sendMail(final Mail mail, final String logoPath) throws MessagingException {
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
        log.info("Mail {} sent to {}.", mail.subject(), mail.receivers());
    }

    @Override
    public String getBodyFromTemplate(final String templateName, final Map<String, Object> content) throws IOException, TemplateException {
        final Template template = freeMarkerConfigurer.getConfiguration().getTemplate(templateName);
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, content);
    }

    private Resource getRessourceFromClassPath(final String path) {
        return resourceLoader.getResource("classpath:" + path);
    }
}
