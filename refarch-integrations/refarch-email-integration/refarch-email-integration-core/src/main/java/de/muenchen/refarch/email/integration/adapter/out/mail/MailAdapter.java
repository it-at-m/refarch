package de.muenchen.refarch.email.integration.adapter.out.mail;

import de.muenchen.refarch.email.api.EmailApi;
import de.muenchen.refarch.email.integration.application.port.out.MailOutPort;
import de.muenchen.refarch.email.model.Mail;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MailAdapter implements MailOutPort {

    private final EmailApi emailApi;

    @Override
    public void sendMail(final Mail mail, final String logoPath) throws MessagingException {
        this.emailApi.sendMail(mail, logoPath);
    }

    @Override
    public String getBodyFromTemplate(final String templateName, final Map<String, Object> content) throws TemplateException, IOException {
        return this.emailApi.getBodyFromTemplate(templateName, content);
    }
}
