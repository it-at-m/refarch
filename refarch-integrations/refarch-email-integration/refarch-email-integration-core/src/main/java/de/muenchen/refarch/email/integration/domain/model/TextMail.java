package de.muenchen.refarch.email.integration.domain.model;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Object contains all the information needed to send a mail.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
public class TextMail extends BasicMail {

    /**
     * Body of the mail.
     */
    @NotBlank(message = "No body given")
    private final String body;

    public TextMail(String receivers, String receiversCc, String receiversBcc, String subject, String body, String replyTo,
            List<String> filePaths) {
        super(receivers, receiversCc, receiversBcc, subject, replyTo, filePaths);
        this.body = body;
    }

}
