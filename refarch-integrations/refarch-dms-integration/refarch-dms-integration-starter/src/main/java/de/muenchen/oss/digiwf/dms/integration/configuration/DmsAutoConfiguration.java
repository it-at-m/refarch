package de.muenchen.oss.digiwf.dms.integration.configuration;

import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.LHMBAI151700GIWSDSoap;
import de.muenchen.refarch.integration.dms.adapter.out.fabasoft.FabasoftAdapter;
import de.muenchen.refarch.integration.dms.adapter.out.fabasoft.FabasoftClientConfiguration;
import de.muenchen.refarch.integration.dms.adapter.out.fabasoft.FabasoftProperties;
import de.muenchen.refarch.integration.dms.adapter.out.s3.S3Adapter;
import de.muenchen.refarch.integration.s3.client.domain.model.SupportedFileExtensions;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFileRepository;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFolderRepository;
import de.muenchen.refarch.integration.dms.application.port.in.CancelObjectInPort;
import de.muenchen.refarch.integration.dms.application.port.in.CreateDocumentInPort;
import de.muenchen.refarch.integration.dms.application.port.in.CreateFileInPort;
import de.muenchen.refarch.integration.dms.application.port.in.CreateProcedureInPort;
import de.muenchen.refarch.integration.dms.application.port.in.DepositObjectInPort;
import de.muenchen.refarch.integration.dms.application.port.in.ReadContentInPort;
import de.muenchen.refarch.integration.dms.application.port.in.ReadMetadataInPort;
import de.muenchen.refarch.integration.dms.application.port.in.SearchFileInPort;
import de.muenchen.refarch.integration.dms.application.port.in.SearchSubjectAreaInPort;
import de.muenchen.refarch.integration.dms.application.port.in.UpdateDocumentInPort;
import de.muenchen.refarch.integration.dms.application.port.out.CancelObjectOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.CreateDocumentOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.CreateFileOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.CreateProcedureOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.DepositObjectOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.ListContentOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.LoadFileOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.ReadContentOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.ReadMetadataOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.SearchFileOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.SearchSubjectAreaOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.TransferContentOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.UpdateDocumentOutPort;
import de.muenchen.refarch.integration.dms.application.usecase.CancelObjectUseCase;
import de.muenchen.refarch.integration.dms.application.usecase.CreateDocumentUseCase;
import de.muenchen.refarch.integration.dms.application.usecase.CreateFileUseCase;
import de.muenchen.refarch.integration.dms.application.usecase.CreateProcedureUseCase;
import de.muenchen.refarch.integration.dms.application.usecase.DepositObjectUseCase;
import de.muenchen.refarch.integration.dms.application.usecase.ReadContentUseCase;
import de.muenchen.refarch.integration.dms.application.usecase.ReadMetadataUseCase;
import de.muenchen.refarch.integration.dms.application.usecase.SearchFileUseCase;
import de.muenchen.refarch.integration.dms.application.usecase.SearchSubjectAreaUseCase;
import de.muenchen.refarch.integration.dms.application.usecase.UpdateDocumentUseCase;
import de.muenchen.refarch.integration.s3.client.service.FileValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@RequiredArgsConstructor
@Import(FabasoftClientConfiguration.class)
@EnableConfigurationProperties({FabasoftProperties.class, DmsProperties.class})
public class DmsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FabasoftAdapter fabasoftAdapter(final FabasoftProperties dmsProperties, final LHMBAI151700GIWSDSoap wsClient) {
        return new FabasoftAdapter(dmsProperties, wsClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public LHMBAI151700GIWSDSoap wsClient(final FabasoftClientConfiguration fabasoftClientConfiguration) {
        return fabasoftClientConfiguration.dmsWsClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public S3Adapter s3Adapter(final DocumentStorageFileRepository documentStorageFileRepository,
            final DocumentStorageFolderRepository documentStorageFolderRepository,
            final FileValidationService fileValidationService) {
        return new S3Adapter(documentStorageFileRepository, documentStorageFolderRepository, fileValidationService);
    }

    /**
     * Offers a {@link java.util.Map} of supported file extensions for this integration in form of a {@link SupportedFileExtensions} object.
     *
     * @param dmsProperties {@link DmsProperties} contains the supported file extensions.
     * @return {@link SupportedFileExtensions} object representing the supported file extensions.
     */
    @Bean
    public SupportedFileExtensions supportedFileExtensions(final DmsProperties dmsProperties) {
        final SupportedFileExtensions supportedFileExtensions = new SupportedFileExtensions();
        supportedFileExtensions.putAll(dmsProperties.getSupportedFileExtensions());
        return supportedFileExtensions;
    }

    @Bean
    @ConditionalOnMissingBean
    public CreateFileInPort createFileInPort(final CreateFileOutPort createFileOutPort) {
        return new CreateFileUseCase(createFileOutPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public CreateProcedureInPort createProcedureInPort(final CreateProcedureOutPort createProcedureOutPort) {
        return new CreateProcedureUseCase(createProcedureOutPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public CreateDocumentInPort createDocumentInPort(
            final CreateDocumentOutPort createDocumentOutPort,
            final LoadFileOutPort loadFileOutPort,
            final ListContentOutPort listContentOutPort
    ) {
        return new CreateDocumentUseCase(createDocumentOutPort, loadFileOutPort, listContentOutPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public UpdateDocumentInPort updateDocumentInPort(
            final UpdateDocumentOutPort updateDocumentOutPort,
            final LoadFileOutPort loadFileOutPort,
            final ListContentOutPort listContentOutPort
    ) {
        return new UpdateDocumentUseCase(updateDocumentOutPort, listContentOutPort, loadFileOutPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public DepositObjectInPort depositObjectInPort(DepositObjectOutPort depositObjectOutPort) {
        return new DepositObjectUseCase(depositObjectOutPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public CancelObjectInPort cancelObjectInPort(CancelObjectOutPort cancelObjectOutPort) {
        return new CancelObjectUseCase(cancelObjectOutPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReadContentInPort readContentInPort(ReadContentOutPort readContentOutPort, TransferContentOutPort transferContentOutPort) {
        return new ReadContentUseCase(transferContentOutPort, readContentOutPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public SearchFileInPort searchFileInPort(SearchFileOutPort searchFileOutPort) {
        return new SearchFileUseCase(searchFileOutPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public SearchSubjectAreaInPort searchSubjectAreaInPort(SearchSubjectAreaOutPort searchSubjectAreaOutPort) {
        return new SearchSubjectAreaUseCase(searchSubjectAreaOutPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReadMetadataInPort readMetadataInPort(ReadMetadataOutPort readMetadataOutPort) {
        return new ReadMetadataUseCase(readMetadataOutPort);
    }
}
