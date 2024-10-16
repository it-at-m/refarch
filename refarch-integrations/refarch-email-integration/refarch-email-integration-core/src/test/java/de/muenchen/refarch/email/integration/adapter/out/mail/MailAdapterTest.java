package de.muenchen.refarch.email.integration.adapter.out.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.refarch.email.api.EmailApi;
import de.muenchen.refarch.email.model.Mail;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MailAdapterTest {

    private final EmailApi emailApi = mock(EmailApi.class);

    @Test
    void sendMail() throws MessagingException {
        final MailAdapter mailAdapter = new MailAdapter(emailApi);
        final Mail mail = new Mail(
                "receivers",
                "receiversCc",
                "receiversBcc",
                "subject",
                "body",
                false,
                null,
                "replyTo",
                null);
        mailAdapter.sendMail(mail, "logoPath");
        verify(emailApi).sendMail(mail, "logoPath");
    }

    @Test
    void testGetBodyFromTemplate() throws TemplateException, IOException {
        final MailAdapter mailAdapter = new MailAdapter(emailApi);
        when(emailApi.getBodyFromTemplate(anyString(), anyMap())).thenReturn("generated body");
        final String body = mailAdapter.getBodyFromTemplate("template", Map.of("key", "value"));

        assertThat(body).isEqualTo("generated body");
        verify(emailApi).getBodyFromTemplate("template", Map.of("key", "value"));
    }

}
