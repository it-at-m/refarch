package de.muenchen.refarch.email.integration.application.port.out;

import de.muenchen.refarch.email.model.Mail;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.util.Map;

public interface MailOutPort {

    void sendMail(Mail mail, String logoPath) throws MessagingException;

    String getBodyFromTemplate(String templateName, Map<String, Object> content) throws TemplateException, IOException;

}
