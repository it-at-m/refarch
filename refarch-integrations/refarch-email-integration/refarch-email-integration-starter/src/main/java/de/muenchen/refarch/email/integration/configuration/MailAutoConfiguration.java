package de.muenchen.refarch.email.integration.configuration;

import de.muenchen.refarch.email.api.EmailApi;
import de.muenchen.refarch.email.integration.adapter.out.mail.MailAdapter;
import de.muenchen.refarch.email.integration.adapter.out.s3.S3Adapter;
import de.muenchen.refarch.email.integration.application.port.in.SendMailInPort;
import de.muenchen.refarch.email.integration.application.port.out.LoadMailAttachmentOutPort;
import de.muenchen.refarch.email.integration.application.port.out.MailOutPort;
import de.muenchen.refarch.email.integration.application.usecase.SendMailUseCase;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFileRepository;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFolderRepository;
import de.muenchen.refarch.integration.s3.client.service.FileValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({ MailProperties.class })
public class MailAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SendMailInPort getSendMailPathsInPort(final LoadMailAttachmentOutPort loadAttachmentPort, final MailOutPort mailOutPort) {
        return new SendMailUseCase(loadAttachmentPort, mailOutPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public LoadMailAttachmentOutPort getLoadMailAttachmentPort(
            final DocumentStorageFileRepository documentStorageFileRepository,
            final DocumentStorageFolderRepository documentStorageFolderRepository,
            final FileValidationService fileValidationService) {
        return new S3Adapter(documentStorageFileRepository, documentStorageFolderRepository, fileValidationService);
    }

    @Bean
    @ConditionalOnMissingBean
    public MailOutPort getMailPort(final EmailApi emailApi) {
        return new MailAdapter(emailApi);
    }
}
