package de.muenchen.oss.refarch.email.integration.application.port.out;

import de.muenchen.oss.refarch.email.integration.domain.exception.SendMailException;
import de.muenchen.oss.refarch.email.integration.domain.exception.TemplateException;
import de.muenchen.oss.refarch.email.integration.domain.model.Mail;
import de.muenchen.oss.refarch.email.integration.domain.model.TemplateMail;
import de.muenchen.oss.refarch.email.integration.domain.model.TextMail;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

/**
 * Outbound port for sending emails.
 */
@Validated
public interface MailOutPort {

    /**
     * Sends a mail as provided by the {@link Mail}
     * model.
     *
     * @param mail the fully constructed mail to send
     * @throws SendMailException if an error occurs while constructing or sending the message
     */
    void sendMail(@NotNull @Valid Mail mail);

    /**
     * Sends a mail and optionally embeds an inline logo image.
     *
     * @param mail the fully constructed mail to send
     * @param logoPath classpath-relative path (without the "classpath:" prefix) of the logo to embed;
     *            if {@code null} or the resource is not found, the mail is sent without a logo
     * @throws SendMailException if an error occurs while constructing or sending the message
     */
    void sendMail(@NotNull @Valid Mail mail, String logoPath);

    /**
     * Sends a plain text mail.
     *
     * @param mail the text mail to send
     * @throws SendMailException if an error occurs while constructing or sending the message
     */
    void sendTextMail(@NotNull @Valid TextMail mail);

    /**
     * Renders and sends an HTML mail based on a template.
     *
     * @param mail the template mail containing the template name and content model
     * @throws SendMailException if an error occurs while constructing or sending the message
     * @throws TemplateException if the template cannot be rendered
     */
    void sendHtmlMailWithTemplate(@NotNull @Valid TemplateMail mail);

    /**
     * Renders and sends an HTML mail based on a template and optionally embeds an inline logo image.
     *
     * @param mail the template mail containing the template name and content model
     * @param logoPath classpath-relative path (without the "classpath:" prefix) of the logo to embed;
     *            if {@code null} or the resource is not found, the mail is sent without a logo
     * @throws SendMailException if an error occurs while constructing or sending the message
     * @throws TemplateException if the template cannot be rendered
     */
    void sendHtmlMailWithTemplate(@NotNull @Valid TemplateMail mail, String logoPath);
}
