package de.muenchen.refarch.email.integration.adapter.out.mail;

import de.muenchen.refarch.email.integration.domain.exception.SendMailException;
import de.muenchen.refarch.email.integration.domain.exception.TemplateException;
import de.muenchen.refarch.email.integration.domain.model.Attachment;
import de.muenchen.refarch.email.integration.domain.model.Mail;
import de.muenchen.refarch.email.integration.domain.model.TemplateMail;
import de.muenchen.refarch.email.integration.domain.model.TextMail;
import freemarker.template.Configuration;
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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

class MailAdapterTest {

    public static final String ERROR_WHILE_RENDERING_TEMPLATE = "Error while rendering template";
    private final JavaMailSender javaMailSender = Mockito.mock(JavaMailSender.class);
    private final ResourceLoader resourceLoader = Mockito.mock(ResourceLoader.class);
    private final FreeMarkerConfigurer freeMarkerConfigurer = Mockito.mock(FreeMarkerConfigurer.class);
    // test data
    private static final String RECEIVER = "mailReceiver1@muenchen.de,mailReceiver2@muenchen.de";
    private static final String RECEIVER_CC = "receiverCC@muenchen.de";
    private static final String RECEIVER_BCC = "receiverBCC@muenchen.de";
    private static final String SUBJECT = "Test Mail";
    private static final String BODY = "This is a test mail";
    private static final String REPLY_TO = "test@muenchen.de";
    private static final String DEFAULT_REPLY_TO = "noreply@muenchen.de";
    private static final String SENDER = "some-custom-sender@muenchen.de";
    private MailAdapter mailAdapter;

    @BeforeEach
    void setUp() {
        Mockito.when(this.javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        this.mailAdapter = new MailAdapter(this.javaMailSender, this.resourceLoader, freeMarkerConfigurer, "test@muenchen.de", DEFAULT_REPLY_TO);
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
        this.mailAdapter.sendMail(mail);

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
        new MailAdapter(this.javaMailSender, this.resourceLoader, freeMarkerConfigurer, customAddress.getAddress(), null).sendMail(mail);

        assertMailSend(new InternetAddress(customAddress.getAddress()));
    }

    private void assertMailSend(final InternetAddress replyToAddress) throws MessagingException, IOException {
        final ArgumentCaptor<MimeMessage> messageArgumentCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        Mockito.verify(this.javaMailSender).send(messageArgumentCaptor.capture());

        Assertions.assertThat(messageArgumentCaptor.getValue().getAllRecipients()).hasSize(2);
        Assertions.assertThat(messageArgumentCaptor.getValue().getSubject()).isEqualTo(SUBJECT);
        Assertions.assertThat(messageArgumentCaptor.getValue().getReplyTo()).hasSize(1);
        Assertions.assertThat(messageArgumentCaptor.getValue().getReplyTo()).contains(replyToAddress);
        final MimeMultipart content = (MimeMultipart) messageArgumentCaptor.getValue().getContent();
        Assertions.assertThat(content.getContentType()).contains(MediaType.MULTIPART_MIXED.getType());
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
        this.mailAdapter.sendMail(mail);

        final ArgumentCaptor<MimeMessage> messageArgumentCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        Mockito.verify(this.javaMailSender).send(messageArgumentCaptor.capture());

        Assertions.assertThat(messageArgumentCaptor.getValue().getAllRecipients()).hasSize(4);
        Assertions.assertThat(messageArgumentCaptor.getValue().getAllRecipients()).containsAll(
                List.of(new InternetAddress("mailReceiver1@muenchen.de"), new InternetAddress("mailReceiver2@muenchen.de"),
                        new InternetAddress(RECEIVER_CC), new InternetAddress(RECEIVER_BCC)));
        Assertions.assertThat(messageArgumentCaptor.getValue().getSubject()).isEqualTo(SUBJECT);
        Assertions.assertThat(messageArgumentCaptor.getValue().getReplyTo()).hasSize(1);
        Assertions.assertThat(messageArgumentCaptor.getValue().getReplyTo()).contains(new InternetAddress(REPLY_TO));
        Assertions.assertThat(messageArgumentCaptor.getValue().getFrom()).contains(new InternetAddress(SENDER));
        final MimeMultipart content = (MimeMultipart) messageArgumentCaptor.getValue().getContent();
        Assertions.assertThat(content.getContentType()).contains(MediaType.MULTIPART_MIXED.getType());
    }

    @Test
    void sendMailWithAttachments() throws MessagingException, IOException {
        final ByteArrayDataSource fileContent = new ByteArrayDataSource("FooBar".getBytes(StandardCharsets.UTF_8), "application/pdf");
        final Attachment attachment = new Attachment("Testanhang", fileContent);
        final Mail mail = new Mail(
                RECEIVER,
                null,
                null,
                SUBJECT,
                BODY,
                false,
                null,
                null,
                List.of(attachment));
        this.mailAdapter.sendMail(mail);

        final ArgumentCaptor<MimeMessage> messageArgumentCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        Mockito.verify(this.javaMailSender).send(messageArgumentCaptor.capture());

        Assertions.assertThat(messageArgumentCaptor.getValue().getAllRecipients()).hasSize(2);
        Assertions.assertThat(messageArgumentCaptor.getValue().getSubject()).isEqualTo(SUBJECT);
        final MimeMultipart content = (MimeMultipart) messageArgumentCaptor.getValue().getContent();
        Assertions.assertThat(content.getContentType()).contains(MediaType.MULTIPART_MIXED.getType());
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
        this.mailAdapter.sendMail(mail);

        final ArgumentCaptor<MimeMessage> messageArgumentCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        Mockito.verify(this.javaMailSender).send(messageArgumentCaptor.capture());

        Assertions.assertThat(messageArgumentCaptor.getValue().getAllRecipients()).hasSize(2);
        Assertions.assertThat(messageArgumentCaptor.getValue().getSubject()).isEqualTo(SUBJECT);
        Assertions.assertThat(messageArgumentCaptor.getValue().getReplyTo()).hasSize(2);
        Assertions.assertThat(messageArgumentCaptor.getValue().getReplyTo()).containsAll(List.of(reply1, reply2));
        final MimeMultipart content = (MimeMultipart) messageArgumentCaptor.getValue().getContent();
        Assertions.assertThat(content.getContentType()).contains(MediaType.MULTIPART_MIXED.getType());
    }

    @Test
    void sendMailWithDefaultLogo() throws MessagingException, IOException {
        Mockito.when(this.resourceLoader.getResource(ArgumentMatchers.anyString())).thenReturn(this.getResourceForText("Default Logo", true));

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
        this.mailAdapter.sendMail(mail, "bausteine/mail/email-logo.png");

        final ArgumentCaptor<MimeMessage> messageArgumentCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        Mockito.verify(this.javaMailSender).send(messageArgumentCaptor.capture());
        Mockito.verify(this.resourceLoader).getResource("classpath:bausteine/mail/email-logo.png");

        Assertions.assertThat(messageArgumentCaptor.getValue().getAllRecipients()).hasSize(2);
        Assertions.assertThat(messageArgumentCaptor.getValue().getSubject()).isEqualTo(SUBJECT);
        final MimeMultipart content = (MimeMultipart) messageArgumentCaptor.getValue().getContent();
        Assertions.assertThat(content.getContentType()).contains(MediaType.MULTIPART_MIXED.getType());
    }

    @Test
    void sendMailWithCustomLogo() throws MessagingException, IOException {
        Mockito.when(this.resourceLoader.getResource(ArgumentMatchers.anyString())).thenReturn(this.getResourceForText("Custom Logo", true));

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
        this.mailAdapter.sendMail(mail, "some/random/path/on/classpath");

        final ArgumentCaptor<MimeMessage> messageArgumentCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        Mockito.verify(this.javaMailSender).send(messageArgumentCaptor.capture());
        Mockito.verify(this.resourceLoader).getResource("classpath:some/random/path/on/classpath");

        Assertions.assertThat(messageArgumentCaptor.getValue().getAllRecipients()).hasSize(2);
        Assertions.assertThat(messageArgumentCaptor.getValue().getSubject()).isEqualTo(SUBJECT);
        final MimeMultipart content = (MimeMultipart) messageArgumentCaptor.getValue().getContent();
        Assertions.assertThat(content.getContentType()).contains(MediaType.MULTIPART_MIXED.getType());
    }

    @Test
    void testGetBodyFromFreemarkerTemplate() {
        final String templateName = "test-template.ftl";
        final Map<String, Object> content = Map.of("data", "test");
        final Configuration configuration = new Configuration(Configuration.VERSION_2_3_30);
        configuration.setClassForTemplateLoading(this.getClass(), "/templates/");
        Mockito.when(this.freeMarkerConfigurer.getConfiguration()).thenReturn(configuration);

        final String result = this.mailAdapter.getBodyFromTemplate(templateName, content);

        Assertions.assertThat(result.contains("test")).isTrue();
    }

    private Resource getResourceForText(final String text, final boolean resourceExists) {
        return new Resource() {
            @Override
            public boolean exists() {
                return resourceExists;
            }

            @Override
            public URL getURL() {
                return null;
            }

            @Override
            public URI getURI() {
                return null;
            }

            @Override
            public File getFile() {
                return null;
            }

            @Override
            public long contentLength() {
                return 0;
            }

            @Override
            public long lastModified() {
                return 0;
            }

            @Override
            public Resource createRelative(String relativePath) {
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
            public InputStream getInputStream() {
                return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
            }
        };
    }

    @Test
    void sendTextMailWrapsMessagingException() {
        final TextMail textMail = new TextMail(
                RECEIVER,
                RECEIVER_CC,
                RECEIVER_BCC,
                SUBJECT,
                BODY,
                REPLY_TO,
                List.of());

        Mockito.doThrow(new MailSendException("Test Exception"))
                .when(this.javaMailSender)
                .send(ArgumentMatchers.any(MimeMessage.class));

        Assertions.assertThatThrownBy(() -> this.mailAdapter.sendTextMail(textMail))
                .isInstanceOf(SendMailException.class);
    }

    @Test
    void sendHtmlMailWithTemplateThrowsIOException() {
        final TemplateMail templateMail = new TemplateMail(
                RECEIVER,
                null,
                null,
                SUBJECT,
                REPLY_TO,
                List.of(),
                "template.ftl",
                Map.of("subject", SUBJECT));
        final MailAdapter spyAdapter = Mockito.spy(this.mailAdapter);
        Mockito.doThrow(new TemplateException(
                ERROR_WHILE_RENDERING_TEMPLATE,
                new IOException("IO Exception")))
                .when(spyAdapter)
                .getBodyFromTemplate(ArgumentMatchers.anyString(), ArgumentMatchers.anyMap());

        Assertions.assertThatThrownBy(() -> spyAdapter.sendHtmlMailWithTemplate(templateMail))
                .isInstanceOf(TemplateException.class)
                .hasMessage(ERROR_WHILE_RENDERING_TEMPLATE);
    }

    @Test
    void sendHtmlMailWithTemplateThrowsTemplateException() {
        final TemplateMail templateMail = new TemplateMail(
                RECEIVER,
                null,
                null,
                SUBJECT,
                REPLY_TO,
                List.of(),
                "template.ftl",
                Map.of("subject", SUBJECT));
        final MailAdapter spyAdapter = Mockito.spy(this.mailAdapter);
        Mockito.doThrow(new TemplateException(ERROR_WHILE_RENDERING_TEMPLATE, new RuntimeException("test")))
                .when(spyAdapter)
                .getBodyFromTemplate(ArgumentMatchers.anyString(), ArgumentMatchers.anyMap());

        Assertions.assertThatThrownBy(() -> spyAdapter.sendHtmlMailWithTemplate(templateMail))
                .isInstanceOf(TemplateException.class)
                .hasMessage(ERROR_WHILE_RENDERING_TEMPLATE);
    }

}
