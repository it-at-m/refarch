package de.muenchen.refarch.email;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.refarch.email.api.EmailApi;
import de.muenchen.refarch.email.impl.EmailApiImpl;
import de.muenchen.refarch.email.model.FileAttachment;
import de.muenchen.refarch.email.model.Mail;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

class EmailApiImplTest {

    private final JavaMailSender javaMailSender = mock(JavaMailSender.class);
    private final ResourceLoader resourceLoader = mock(ResourceLoader.class);
    private final FreeMarkerConfigurer freeMarkerConfigurer = mock(FreeMarkerConfigurer.class);
    // test data
    private final String receiver = "mailReceiver1@muenchen.de,mailReceiver2@muenchen.de";
    private final String receiverCC = "receiverCC@muenchen.de";
    private final String receiverBCC = "receiverBCC@muenchen.de";
    private final String subject = "Test Mail";
    private final String body = "This is a test mail";
    private final String replyTo = "test@muenchen.de";
    private final String defaultReplyTo = "noreply@muenchen.de";
    private final String sender = "some-custom-sender@muenchen.de";
    private EmailApi emailApi;

    @BeforeEach
    void setUp() {
        when(this.javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        this.emailApi = new EmailApiImpl(this.javaMailSender, this.resourceLoader, freeMarkerConfigurer, "test@muenchen.de", defaultReplyTo);
    }

    @Test
    void testSendMail() throws MessagingException, IOException {
        final Mail mail = new Mail(
                this.receiver,
                null,
                null,
                this.subject,
                this.body,
                false,
                null,
                null,
                null
        );
        this.emailApi.sendMail(mail);

        final ArgumentCaptor<MimeMessage> messageArgumentCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(this.javaMailSender).send(messageArgumentCaptor.capture());

        assertThat(messageArgumentCaptor.getValue().getAllRecipients()).hasSize(2);
        assertThat(messageArgumentCaptor.getValue().getSubject()).isEqualTo(this.subject);
        assertThat(messageArgumentCaptor.getValue().getReplyTo()).hasSize(1);
        assertThat(messageArgumentCaptor.getValue().getReplyTo()).contains(new InternetAddress(this.defaultReplyTo));
        final MimeMultipart content = (MimeMultipart) messageArgumentCaptor.getValue().getContent();
        assertThat(content.getContentType()).contains("multipart/mixed");
    }

    @Test
    void testSendMailNoDefaultReplyTo() throws MessagingException, IOException {
        var customAddress = new InternetAddress("custom.test@muenchen.de");

        final Mail mail = new Mail(
                this.receiver,
                null,
                null,
                this.subject,
                this.body,
                false,
                null,
                null,
                null
        );
        new EmailApiImpl(this.javaMailSender, this.resourceLoader, freeMarkerConfigurer, customAddress.getAddress(), null).sendMail(mail);

        final ArgumentCaptor<MimeMessage> messageArgumentCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(this.javaMailSender).send(messageArgumentCaptor.capture());

        assertThat(messageArgumentCaptor.getValue().getAllRecipients()).hasSize(2);
        assertThat(messageArgumentCaptor.getValue().getSubject()).isEqualTo(this.subject);
        assertThat(messageArgumentCaptor.getValue().getReplyTo()).hasSize(1);
        assertThat(messageArgumentCaptor.getValue().getReplyTo()).contains(new InternetAddress(customAddress.getAddress()));
        final MimeMultipart content = (MimeMultipart) messageArgumentCaptor.getValue().getContent();
        assertThat(content.getContentType()).contains("multipart/mixed");
    }

    @Test
    void testSendMailWithOptions() throws MessagingException, IOException {
        final Mail mail = new Mail(
                this.receiver,
                this.receiverCC,
                this.receiverBCC,
                this.subject,
                this.body,
                false,
                this.sender,
                this.replyTo,
                null
        );
        this.emailApi.sendMail(mail);

        final ArgumentCaptor<MimeMessage> messageArgumentCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(this.javaMailSender).send(messageArgumentCaptor.capture());

        assertThat(messageArgumentCaptor.getValue().getAllRecipients()).hasSize(4);
        assertThat(messageArgumentCaptor.getValue().getAllRecipients()).containsAll(
                List.of(new InternetAddress("mailReceiver1@muenchen.de"), new InternetAddress("mailReceiver2@muenchen.de"),
                        new InternetAddress(this.receiverCC), new InternetAddress(this.receiverBCC)));
        assertThat(messageArgumentCaptor.getValue().getSubject()).isEqualTo(this.subject);
        assertThat(messageArgumentCaptor.getValue().getReplyTo()).hasSize(1);
        assertThat(messageArgumentCaptor.getValue().getReplyTo()).contains(new InternetAddress(this.replyTo));
        assertThat(messageArgumentCaptor.getValue().getFrom()).contains(new InternetAddress(this.sender));
        final MimeMultipart content = (MimeMultipart) messageArgumentCaptor.getValue().getContent();
        assertThat(content.getContentType()).contains("multipart/mixed");
    }

    @Test
    void sendMailWithAttachments() throws MessagingException, IOException {
        final Mail mail = new Mail(
                this.receiver,
                null,
                null,
                this.subject,
                this.body,
                false,
                null,
                null,
                List.of(
                        new FileAttachment(
                                "Testanhang",
                                new ByteArrayDataSource("FooBar".getBytes(), "text/plain")
                        )
                )
        );
        this.emailApi.sendMail(mail);

        final ArgumentCaptor<MimeMessage> messageArgumentCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(this.javaMailSender).send(messageArgumentCaptor.capture());

        assertThat(messageArgumentCaptor.getValue().getAllRecipients()).hasSize(2);
        assertThat(messageArgumentCaptor.getValue().getSubject()).isEqualTo(this.subject);
        final MimeMultipart content = (MimeMultipart) messageArgumentCaptor.getValue().getContent();
        assertThat(content.getContentType()).contains("multipart/mixed");
    }

    @Test
    void sendMailWithMultipleReplyToAddresses() throws MessagingException, IOException {
        var reply1 = new InternetAddress("address1@muenchen.de");
        var reply2 = new InternetAddress("address2@muenchen.de");

        final Mail mail = new Mail(
                this.receiver,
                null,
                null,
                this.subject,
                this.body,
                false,
                null,
                reply1.getAddress() + "," + reply2.getAddress(),
                null
        );
        this.emailApi.sendMail(mail);

        final ArgumentCaptor<MimeMessage> messageArgumentCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(this.javaMailSender).send(messageArgumentCaptor.capture());

        assertThat(messageArgumentCaptor.getValue().getAllRecipients()).hasSize(2);
        assertThat(messageArgumentCaptor.getValue().getSubject()).isEqualTo(this.subject);
        assertThat(messageArgumentCaptor.getValue().getReplyTo()).hasSize(2);
        assertThat(messageArgumentCaptor.getValue().getReplyTo()).containsAll(List.of(reply1, reply2));
        final MimeMultipart content = (MimeMultipart) messageArgumentCaptor.getValue().getContent();
        assertThat(content.getContentType()).contains("multipart/mixed");
    }

    @Test
    void sendMailWithDefaultLogo() throws MessagingException, IOException {
        when(this.resourceLoader.getResource(anyString())).thenReturn(this.getResourceForText("Default Logo", true));

        final Mail mail = new Mail(
                this.receiver,
                null,
                null,
                this.subject,
                this.body,
                false,
                null,
                null,
                null
        );
        this.emailApi.sendMailWithDefaultLogo(mail);

        final ArgumentCaptor<MimeMessage> messageArgumentCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(this.javaMailSender).send(messageArgumentCaptor.capture());
        verify(this.resourceLoader).getResource("classpath:bausteine/mail/email-logo.png");

        assertThat(messageArgumentCaptor.getValue().getAllRecipients()).hasSize(2);
        assertThat(messageArgumentCaptor.getValue().getSubject()).isEqualTo(this.subject);
        final MimeMultipart content = (MimeMultipart) messageArgumentCaptor.getValue().getContent();
        assertThat(content.getContentType()).contains("multipart/mixed");
    }

    @Test
    void sendMailWithCustomLogo() throws MessagingException, IOException {
        when(this.resourceLoader.getResource(anyString())).thenReturn(this.getResourceForText("Custom Logo", true));

        final Mail mail = new Mail(
                this.receiver,
                null,
                null,
                this.subject,
                this.body,
                false,
                null,
                null,
                null
        );
        this.emailApi.sendMail(mail, "some/random/path/on/classpath");

        final ArgumentCaptor<MimeMessage> messageArgumentCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(this.javaMailSender).send(messageArgumentCaptor.capture());
        verify(this.resourceLoader).getResource("classpath:some/random/path/on/classpath");

        assertThat(messageArgumentCaptor.getValue().getAllRecipients()).hasSize(2);
        assertThat(messageArgumentCaptor.getValue().getSubject()).isEqualTo(this.subject);
        final MimeMultipart content = (MimeMultipart) messageArgumentCaptor.getValue().getContent();
        assertThat(content.getContentType()).contains("multipart/mixed");
    }

    @Test
    void testGetBodyFromFreemarkerTemplate() throws IOException, TemplateException {
        final String templateName = "test-template.ftl";
        Map<String, Object> content = Map.of("data", "test");
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_30);
        configuration.setClassForTemplateLoading(this.getClass(), "/templates/");
        when(this.freeMarkerConfigurer.getConfiguration()).thenReturn(configuration);

        final String result = this.emailApi.getBodyFromTemplate(templateName, content);

        assertThat(result.contains("test")).isTrue();
    }

    @Test
    void testGetEmailBodyFromTemplate() {
        when(this.resourceLoader.getResource(anyString())).thenReturn(this.getResourceForText("This is a test mail", true));

        final String templatePath = "bausteine/mail/email-logo.png";
        final String result = this.emailApi.getEmailBodyFromTemplate(templatePath, Map.of());

        assertThat(result).isEqualTo("This is a test mail");
    }

    @Test
    void testGetEmailBodyFromTemplateWithContent() {
        when(this.resourceLoader.getResource(anyString())).thenReturn(this.getResourceForText("This is a test mail with content", true));

        final String templatePath = "bausteine/mail/email-logo.png";
        final String result = this.emailApi.getEmailBodyFromTemplate(templatePath, Map.of("content", "some content"));

        assertThat(result).isEqualTo("This is a test mail with some content");
    }

    @Test
    void testGetEmailBodyFromTemplateWithContentAndNewLines() {
        when(this.resourceLoader.getResource(anyString())).thenReturn(this.getResourceForText("This is a test mail with content", true));

        final String templatePath = "bausteine/mail/email-logo.png";
        final String result = this.emailApi.getEmailBodyFromTemplate(templatePath, Map.of("content", "some content \n with new line"));

        assertThat(result).isEqualTo("This is a test mail with some content <br/> with new line");
    }

    @Test
    void testGetEmailBodyFromTemplateWithContentFailsIfTemplateDoesNotExist() {
        when(this.resourceLoader.getResource(anyString())).thenReturn(this.getResourceForText("foo bar", false));

        final String templatePath = "some/temlate/that/does/not/exist";
        assertThatThrownBy(() -> {
            this.emailApi.getEmailBodyFromTemplate(templatePath, Map.of("content", "some content"));
        })
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email Template not found: " + templatePath);
    }

    private Resource getResourceForText(final String text, final boolean resourceExists) {
        return new Resource() {
            @Override
            public boolean exists() {
                return resourceExists;
            }

            @Override
            public URL getURL() throws IOException {
                return null;
            }

            @Override
            public URI getURI() throws IOException {
                return null;
            }

            @Override
            public File getFile() throws IOException {
                return null;
            }

            @Override
            public long contentLength() throws IOException {
                return 0;
            }

            @Override
            public long lastModified() throws IOException {
                return 0;
            }

            @Override
            public Resource createRelative(String relativePath) throws IOException {
                return null;
            }

            @Override
            public String getFilename() {
                return "test.txt";
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(text.getBytes());
            }
        };
    }

}
