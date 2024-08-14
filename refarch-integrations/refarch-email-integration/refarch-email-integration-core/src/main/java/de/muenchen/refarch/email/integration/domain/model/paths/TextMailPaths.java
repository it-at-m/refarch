package de.muenchen.refarch.email.integration.domain.model.paths;

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
public class TextMailPaths extends BasicMailPaths {

    /**
     * Body of the mail.
     */
    @NotBlank(message = "No body given")
    private String body;

    public TextMailPaths(String receivers, String receiversCc, String receiversBcc, String subject, String body, String replyTo,
            List<String> filePaths) {
        super(receivers, receiversCc, receiversBcc, subject, replyTo, filePaths);
        this.body = body;
    }

}
