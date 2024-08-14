package de.muenchen.refarch.email.integration.adapter.out.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import de.muenchen.refarch.email.api.DigiwfEmailApi;
import de.muenchen.refarch.email.model.Mail;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MailAdapterTest {

    private final DigiwfEmailApi digiwfEmailApi = mock(DigiwfEmailApi.class);

    @Test
    void sendMail() throws MessagingException {
        final MailAdapter mailAdapter = new MailAdapter(digiwfEmailApi);
        final Mail mail = Mail.builder()
                .receivers("receivers")
                .subject("subject")
                .body("body")
                .replyTo("replyTo")
                .receiversCc("receiversCc")
                .receiversBcc("receiversBcc")
                .build();
        mailAdapter.sendMail(mail, "logoPath");
        verify(digiwfEmailApi).sendMail(mail, "logoPath");
    }

    @Test
    void getBodyFromTemplate() throws TemplateException, IOException {
        final MailAdapter mailAdapter = new MailAdapter(digiwfEmailApi);
        when(digiwfEmailApi.getBodyFromTemplate(anyString(), anyMap())).thenReturn("generated body");
        String body = mailAdapter.getBodyFromTemplate("template", Map.of("key", "value"));

        assertThat(body).isEqualTo("generated body");
        verify(digiwfEmailApi).getBodyFromTemplate("template", Map.of("key", "value"));
    }

}
