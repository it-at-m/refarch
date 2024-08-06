package de.muenchen.refarch.email.integration.adapter.out.mail;

import de.muenchen.refarch.email.api.DigiwfEmailApi;
import de.muenchen.refarch.email.integration.application.port.out.MailOutPort;
import de.muenchen.refarch.email.model.Mail;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MailAdapter implements MailOutPort {

    private final DigiwfEmailApi digiwfEmailApi;

    @Override
    public void sendMail(Mail mail, String logoPath) throws MessagingException {
        this.digiwfEmailApi.sendMail(mail, logoPath);
    }

    @Override
    public String getBodyFromTemplate(String templateName, Map<String, Object> content) throws TemplateException, IOException {
        return this.digiwfEmailApi.getBodyFromTemplate(templateName, content);
    }
}
