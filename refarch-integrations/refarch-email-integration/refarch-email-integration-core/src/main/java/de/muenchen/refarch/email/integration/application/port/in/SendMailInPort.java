package de.muenchen.refarch.email.integration.application.port.in;

import de.muenchen.refarch.email.integration.domain.model.TemplateMail;
import de.muenchen.refarch.email.integration.domain.model.TextMail;
import jakarta.validation.Valid;

public interface SendMailInPort {

    /**
     * Send a mail.
     *
     * @param mail Mail that is sent
     */
    void sendMailWithText(@Valid TextMail mail);

    /**
     * Send a mail.
     *
     * @param mail Mail that is sent
     * @param logoPath Path to logo in class path which is injected into mail.
     */
    void sendMailWithText(@Valid TextMail mail, String logoPath);

    void sendMailWithTemplate(@Valid TemplateMail mail);

    void sendMailWithTemplate(@Valid TemplateMail mail, String logoPath);

}
