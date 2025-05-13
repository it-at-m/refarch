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
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

class EmailApiImplTest {

    private final JavaMailSender javaMailSender = mock(JavaMailSender.class);
    private final ResourceLoader resourceLoader = mock(ResourceLoader.class);
    private final FreeMarkerConfigurer freeMarkerConfigurer = mock(FreeMarkerConfigurer.class);
    // test data
    private static final String RECEIVER = "mailReceiver1@muenchen.de,mailReceiver2@muenchen.de";
    private static final String RECEIVER_CC = "receiverCC@muenchen.de";
    private static final String RECEIVER_BCC = "receiverBCC@muenchen.de";
    private static final String SUBJECT = "Test Mail";
    private static final String BODY = "This is a test mail";
    private static final String REPLY_TO = "test@muenchen.de";
    private static final String DEFAULT_REPLY_TO = "noreply@muenchen.de";
    private static final String SENDER = "some-custom-sender@muenchen.de";
    private EmailApi emailApi;

    @BeforeEach
    void setUp() {
        when(this.javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        this.emailApi = new EmailApiImpl(this.javaMailSender, this.resourceLoader, freeMarkerConfigurer, "test@muenchen.de", DEFAULT_REPLY_TO);
    }

    @Test
    void testSendMail() throws MessagingException, IOException {
        final Mail mail = new Mail(
                RECEIVER,
                null,
                null,
                SUBJECT,
                BODY,
                false,
                null,
                null,
                null);
        this.emailApi.sendMail(mail);

        assertMailSend(new InternetAddress(DEFAULT_REPLY_TO));
    }

    @Test
    void testSendMailNoDefaultReplyTo() throws MessagingException, IOException {
        final InternetAddress customAddress = new InternetAddress("custom.test@muenchen.de");

        final Mail mail = new Mail(
                RECEIVER,
                null,
                null,
                SUBJECT,
                BODY,
                false,
                null,
                null,
                null);
        new EmailApiImpl(this.javaMailSender, this.resourceLoader, freeMarkerConfigurer, customAddress.getAddress(), null).sendMail(mail);

        assertMailSend(new InternetAddress(customAddress.getAddress()));
    }

    private void assertMailSend(final InternetAddress replyToAddress) throws MessagingException, IOException {
        final ArgumentCaptor<MimeMessage> messageArgumentCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(this.javaMailSender).send(messageArgumentCaptor.capture());

        assertThat(messageArgumentCaptor.getValue().getAllRecipients()).hasSize(2);
        assertThat(messageArgumentCaptor.getValue().getSubject()).isEqualTo(SUBJECT);
        assertThat(messageArgumentCaptor.getValue().getReplyTo()).hasSize(1);
        assertThat(messageArgumentCaptor.getValue().getReplyTo()).contains(replyToAddress);
        final MimeMultipart content = (MimeMultipart) messageArgumentCaptor.getValue().getContent();
        assertThat(content.getContentType()).contains(MediaType.MULTIPART_MIXED.getType());
    }

    @Test
    void testSendMailWithOptions() throws MessagingException, IOException {
        final Mail mail = new Mail(
                RECEIVER,
                RECEIVER_CC,
                RECEIVER_BCC,
                SUBJECT,
                BODY,
                false,
                SENDER,
                REPLY_TO,
                null);
        this.emailApi.sendMail(mail);

        final ArgumentCaptor<MimeMessage> messageArgumentCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(this.javaMailSender).send(messageArgumentCaptor.capture());

        assertThat(messageArgumentCaptor.getValue().getAllRecipients()).hasSize(4);
        assertThat(messageArgumentCaptor.getValue().getAllRecipients()).containsAll(
                List.of(new InternetAddress("mailReceiver1@muenchen.de"), new InternetAddress("mailReceiver2@muenchen.de"),
                        new InternetAddress(RECEIVER_CC), new InternetAddress(RECEIVER_BCC)));
        assertThat(messageArgumentCaptor.getValue().getSubject()).isEqualTo(SUBJECT);
        assertThat(messageArgumentCaptor.getValue().getReplyTo()).hasSize(1);
        assertThat(messageArgumentCaptor.getValue().getReplyTo()).contains(new InternetAddress(REPLY_TO));
        assertThat(messageArgumentCaptor.getValue().getFrom()).contains(new InternetAddress(SENDER));
        final MimeMultipart content = (MimeMultipart) messageArgumentCaptor.getValue().getContent();
        assertThat(content.getContentType()).contains(MediaType.MULTIPART_MIXED.getType());
    }

    @Test
    void sendMailWithAttachments() throws MessagingException, IOException {
        final Mail mail = new Mail(
                RECEIVER,
                null,
                null,
                SUBJECT,
                BODY,
                false,
                null,
                null,
                List.of(
                        new FileAttachment(
                                "Testanhang",
                                new ByteArrayDataSource("FooBar".getBytes(), "text/plain"))));
        this.emailApi.sendMail(mail);

        final ArgumentCaptor<MimeMessage> messageArgumentCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(this.javaMailSender).send(messageArgumentCaptor.capture());

        assertThat(messageArgumentCaptor.getValue().getAllRecipients()).hasSize(2);
        assertThat(messageArgumentCaptor.getValue().getSubject()).isEqualTo(SUBJECT);
        final MimeMultipart content = (MimeMultipart) messageArgumentCaptor.getValue().getContent();
        assertThat(content.getContentType()).contains(MediaType.MULTIPART_MIXED.getType());
    }

    @Test
    void sendMailWithMultipleReplyToAddresses() throws MessagingException, IOException {
        final InternetAddress reply1 = new InternetAddress("address1@muenchen.de");
        final InternetAddress reply2 = new InternetAddress("address2@muenchen.de");

        final Mail mail = new Mail(
                RECEIVER,
                null,
                null,
                SUBJECT,
                BODY,
                false,
                null,
                reply1.getAddress() + "," + reply2.getAddress(),
                null);
        this.emailApi.sendMail(mail);

        final ArgumentCaptor<MimeMessage> messageArgumentCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(this.javaMailSender).send(messageArgumentCaptor.capture());

        assertThat(messageArgumentCaptor.getValue().getAllRecipients()).hasSize(2);
        assertThat(messageArgumentCaptor.getValue().getSubject()).isEqualTo(SUBJECT);
        assertThat(messageArgumentCaptor.getValue().getReplyTo()).hasSize(2);
        assertThat(messageArgumentCaptor.getValue().getReplyTo()).containsAll(List.of(reply1, reply2));
        final MimeMultipart content = (MimeMultipart) messageArgumentCaptor.getValue().getContent();
        assertThat(content.getContentType()).contains(MediaType.MULTIPART_MIXED.getType());
    }

    @Test
    void sendMailWithDefaultLogo() throws MessagingException, IOException {
        when(this.resourceLoader.getResource(anyString())).thenReturn(this.getResourceForText("Default Logo", true));

        final Mail mail = new Mail(
                RECEIVER,
                null,
                null,
                SUBJECT,
                BODY,
                false,
                null,
                null,
                null);
        this.emailApi.sendMailWithDefaultLogo(mail);

        final ArgumentCaptor<MimeMessage> messageArgumentCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(this.javaMailSender).send(messageArgumentCaptor.capture());
        verify(this.resourceLoader).getResource("classpath:bausteine/mail/email-logo.png");

        assertThat(messageArgumentCaptor.getValue().getAllRecipients()).hasSize(2);
        assertThat(messageArgumentCaptor.getValue().getSubject()).isEqualTo(SUBJECT);
        final MimeMultipart content = (MimeMultipart) messageArgumentCaptor.getValue().getContent();
        assertThat(content.getContentType()).contains(MediaType.MULTIPART_MIXED.getType());
    }

    @Test
    void sendMailWithCustomLogo() throws MessagingException, IOException {
        when(this.resourceLoader.getResource(anyString())).thenReturn(this.getResourceForText("Custom Logo", true));

        final Mail mail = new Mail(
                RECEIVER,
                null,
                null,
                SUBJECT,
                BODY,
                false,
                null,
                null,
                null);
        this.emailApi.sendMail(mail, "some/random/path/on/classpath");

        final ArgumentCaptor<MimeMessage> messageArgumentCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(this.javaMailSender).send(messageArgumentCaptor.capture());
        verify(this.resourceLoader).getResource("classpath:some/random/path/on/classpath");

        assertThat(messageArgumentCaptor.getValue().getAllRecipients()).hasSize(2);
        assertThat(messageArgumentCaptor.getValue().getSubject()).isEqualTo(SUBJECT);
        final MimeMultipart content = (MimeMultipart) messageArgumentCaptor.getValue().getContent();
        assertThat(content.getContentType()).contains(MediaType.MULTIPART_MIXED.getType());
    }

    @Test
    void testGetBodyFromFreemarkerTemplate() throws IOException, TemplateException {
        final String templateName = "test-template.ftl";
        final Map<String, Object> content = Map.of("data", "test");
        final Configuration configuration = new Configuration(Configuration.VERSION_2_3_30);
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
