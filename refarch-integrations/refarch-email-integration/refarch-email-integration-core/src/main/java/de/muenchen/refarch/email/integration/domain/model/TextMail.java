package de.muenchen.refarch.email.integration.domain.model;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * Object contains all the information needed to send a mail.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class TextMail extends BasicMail {

    /**
     * Body of the mail.
     */
    @NotBlank(message = "No body given")
    private String body;

    public TextMail(String receivers, String receiversCc, String receiversBcc, String subject, String body, String replyTo,
            List<String> filePaths) {
        super(receivers, receiversCc, receiversBcc, subject, replyTo, filePaths);
        this.body = body;
    }

}
