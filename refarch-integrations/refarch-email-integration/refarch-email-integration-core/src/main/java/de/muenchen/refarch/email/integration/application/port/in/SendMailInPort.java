package de.muenchen.refarch.email.integration.application.port.in;

import de.muenchen.refarch.email.integration.domain.model.TemplateMail;
import de.muenchen.refarch.email.integration.domain.model.TextMail;
import jakarta.validation.Valid;

public interface SendMailInPort {

    void sendMailWithText(@Valid final TextMail mail);

    void sendMailWithTemplate(@Valid final TemplateMail mail);

}
