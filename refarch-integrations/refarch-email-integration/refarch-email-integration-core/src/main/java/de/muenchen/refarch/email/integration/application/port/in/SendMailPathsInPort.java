package de.muenchen.refarch.email.integration.application.port.in;

import de.muenchen.refarch.email.integration.domain.model.paths.TemplateMailPaths;
import de.muenchen.refarch.email.integration.domain.model.paths.TextMailPaths;
import jakarta.validation.Valid;

public interface SendMailPathsInPort {

    void sendMailWithText(@Valid final TextMailPaths mail);

    void sendMailWithTemplate(@Valid final TemplateMailPaths mail);

}
