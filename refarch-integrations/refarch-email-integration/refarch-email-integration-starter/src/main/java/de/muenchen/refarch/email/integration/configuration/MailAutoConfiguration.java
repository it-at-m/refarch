package de.muenchen.refarch.email.integration.configuration;

import de.muenchen.refarch.email.integration.adapter.out.mail.MailAdapter;
import de.muenchen.refarch.email.integration.application.port.in.SendMailInPort;
import de.muenchen.refarch.email.integration.application.port.out.MailOutPort;
import de.muenchen.refarch.email.integration.application.usecase.SendMailUseCase;
import jakarta.mail.MessagingException;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties({ MailProperties.class, CustomMailProperties.class })
public class MailAutoConfiguration {
    private final MailProperties mailProperties;
    private final CustomMailProperties customMailProperties;

    /**
     * Configures the {@link JavaMailSender}
     *
     * @return configured JavaMailSender
     */
    @Bean
    @ConditionalOnMissingBean
    public JavaMailSender getJavaMailSender() throws MessagingException {
        final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(this.mailProperties.getHost());
        mailSender.setPort(this.mailProperties.getPort());
        mailSender.setProtocol(this.mailProperties.getProtocol());
        mailSender.setUsername(this.mailProperties.getUsername());
        mailSender.setPassword(this.mailProperties.getPassword());

        final Properties props = mailSender.getJavaMailProperties();
        props.putAll(this.mailProperties.getProperties());
        mailSender.setJavaMailProperties(props);
        if (customMailProperties.isTestConnection()) {
            mailSender.testConnection();
        }
        return mailSender;
    }

    @Bean
    @ConditionalOnMissingBean
    public FreeMarkerConfigurer freemarkerConfig() {
        final FreeMarkerConfigurer freeMarkerConfigurer = new FreeMarkerConfigurer();
        freeMarkerConfigurer.setTemplateLoaderPath("classpath:templates/");
        return freeMarkerConfigurer;
    }

    @Bean
    @ConditionalOnMissingBean
    public SendMailInPort getSendMailPathsInPort(final MailOutPort mailOutPort) {
        return new SendMailUseCase(mailOutPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public MailOutPort getMailPort(final ResourceLoader resourceLoader, final JavaMailSender javaMailSender,
            final FreeMarkerConfigurer freeMarkerConfigurer) {
        return new MailAdapter(javaMailSender, resourceLoader, freeMarkerConfigurer, this.customMailProperties.getFromAddress(),
                this.customMailProperties.getDefaultReplyToAddress());
    }
}
