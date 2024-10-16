package de.muenchen.refarch.email.impl;

import de.muenchen.refarch.email.api.EmailApi;
import de.muenchen.refarch.email.model.FileAttachment;
import de.muenchen.refarch.email.model.Mail;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

@Slf4j
@RequiredArgsConstructor
public class EmailApiImpl implements EmailApi {

    private final JavaMailSender mailSender;
    private final ResourceLoader resourceLoader;
    private final FreeMarkerConfigurer freeMarkerConfigurer;
    private final String fromAddress;
    private final String defaultReplyToAddress;
    // use a prepackaged sanitizer to prevent XSS
    private final PolicyFactory policy = Sanitizers.BLOCKS
            .and(Sanitizers.FORMATTING)
            .and(Sanitizers.LINKS)
            .and(Sanitizers.TABLES);

    @Override
    public void sendMail(@Valid final Mail mail) throws MessagingException {
        this.sendMail(mail, null);
    }

    @Override
    public void sendMailWithDefaultLogo(@Valid final Mail mail) throws MessagingException {
        this.sendMail(mail, "bausteine/mail/email-logo.png");
    }

    @Override
    public void sendMail(@Valid final Mail mail, final String logoPath) throws MessagingException {
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
            for (final FileAttachment attachment : mail.attachments()) {
                helper.addAttachment(attachment.fileName(), attachment.file());
            }
        }

        // logo
        if (logoPath != null) {
            final Resource logo = this.getRessourceFromClassPath(logoPath);
            helper.addInline("logo", logo);
        }

        this.mailSender.send(mimeMessage);
        log.info("Mail {} sent to {}.", mail.subject(), mail.receivers());
    }

    @Override
    public String getBodyFromTemplate(final String templateName, final Map<String, Object> content) throws IOException, TemplateException {
        final Template template = freeMarkerConfigurer.getConfiguration().getTemplate(templateName);
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, content);
    }

    @Override
    public String getEmailBodyFromTemplate(final String templatePath, final Map<String, String> content) {
        String mailTemplate = this.getTemplate(templatePath);
        for (final Map.Entry<String, String> entry : content.entrySet()) {
            // make sure inputs are sanitized to prevent XSS
            final String value = policy.sanitize(entry.getValue());
            // Make sure new lines are converted to <br> tags
            mailTemplate = mailTemplate.replaceAll(entry.getKey(), value.replaceAll("(\r\n|\n\r|\r|\n)", "<br/>"));
        }
        return mailTemplate;
    }

    private String getTemplate(final String templatePath) {
        final Resource resource = this.getRessourceFromClassPath(templatePath);
        if (!resource.exists()) {
            log.error("Email Template not found: {}", templatePath);
            throw new RuntimeException("Email Template not found: " + templatePath);
        }

        try {
            final byte[] byteArray = FileCopyUtils.copyToByteArray(resource.getInputStream());
            return new String(byteArray, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Failed to load file: {}", templatePath);
            throw new RuntimeException("Failed to load file: " + templatePath, e);
        }
    }

    private Resource getRessourceFromClassPath(final String path) {
        return resourceLoader.getResource("classpath:" + path);
    }
}
