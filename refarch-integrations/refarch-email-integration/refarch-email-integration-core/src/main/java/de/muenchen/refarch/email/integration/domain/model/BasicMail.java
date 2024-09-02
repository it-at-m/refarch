package de.muenchen.refarch.email.integration.domain.model;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BasicMail {
    /**
     * Receiver addresses of the mail, comma separated.
     */
    @NotBlank(message = "No receivers given")
    private String receivers;

    /**
     * CC-Receiver addresses of the mail, comma separated.
     */
    private String receiversCc;

    /**
     * BCC-Receiver addresses of the mail, comma separated.
     */
    private String receiversBcc;

    /**
     * Subject of the mail.
     */
    @NotBlank(message = "No subject given")
    private String subject;

    /**
     * Reply to address
     */
    private String replyTo;
    /**
     * List of attachment paths.
     */
    private List<String> filePaths;
}
