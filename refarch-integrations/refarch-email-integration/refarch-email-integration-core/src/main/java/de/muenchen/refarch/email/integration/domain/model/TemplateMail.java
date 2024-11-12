package de.muenchen.refarch.email.integration.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
@Getter
public class TemplateMail extends BasicMail {

    /**
     * Template of the mail.
     */
    @NotBlank(message = "No template given")
    private final String template;

    /**
     * Bottom body of the mail.
     */
    @NotEmpty(message = "No content given")
    private final Map<String, Object> content;

    public TemplateMail(final String receivers, final String receiversCc, final String receiversBcc, final String subject, final String replyTo,
            final List<String> filePaths,
            final String template, final Map<String, Object> content) {
        super(receivers, receiversCc, receiversBcc, subject, replyTo, filePaths);
        this.template = template;
        this.content = content;
    }
}
