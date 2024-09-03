package de.muenchen.oss.digiwf.dms.integration.configuration;

import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.LHMBAI151700GIWSDSoap;
import de.muenchen.refarch.integration.dms.adapter.out.fabasoft.FabasoftAdapter;
import de.muenchen.refarch.integration.dms.adapter.out.fabasoft.FabasoftClientConfiguration;
import de.muenchen.refarch.integration.dms.adapter.out.fabasoft.FabasoftProperties;
import de.muenchen.refarch.integration.dms.adapter.out.s3.S3Adapter;
import de.muenchen.oss.digiwf.message.process.api.ErrorApi;
import de.muenchen.oss.digiwf.message.process.api.ProcessApi;
import de.muenchen.oss.digiwf.process.api.config.api.ProcessConfigApi;
import de.muenchen.oss.digiwf.s3.integration.client.properties.SupportedFileExtensions;
import de.muenchen.oss.digiwf.s3.integration.client.repository.DocumentStorageFileRepository;
import de.muenchen.oss.digiwf.s3.integration.client.repository.DocumentStorageFolderRepository;
import de.muenchen.oss.digiwf.s3.integration.client.service.FileService;
import de.muenchen.oss.digiwf.s3.integration.client.service.S3DomainProvider;
import de.muenchen.oss.digiwf.s3.integration.client.service.S3StorageUrlProvider;
import de.muenchen.oss.digiwf.spring.security.authentication.UserAuthenticationProvider;
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
import de.muenchen.refarch.integration.dms.application.port.out.DmsUserOutPort;
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
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

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

    /**
     * Constructs an {@link S3DomainProvider} instance specifically tailored for this integration to retrieve the domain-specific S3 storage URL for a given
     * process if its process configuration contains a value for
     * {@link de.muenchen.oss.digiwf.process.api.config.ProcessConfigConstants#APP_FILE_S3_SYNC_CONFIG}.
     *
     * @param processConfigApi {@link ProcessConfigApi} offers access to a process configuration for a given process definition id.
     * @return S3DomainProvider {@link S3DomainProvider} that retrieves the domain-specific S3 storage url for a process if configured.
     */
    @Bean
    public S3DomainProvider s3DomainProvider(final ProcessConfigApi processConfigApi) {
        return processConfigApi::getAppFileS3SyncConfig;
    }

    @Bean
    @ConditionalOnMissingBean
    public S3Adapter s3Adapter(final DocumentStorageFileRepository documentStorageFileRepository,
                               final DocumentStorageFolderRepository documentStorageFolderRepository, final FileService fileService,
                               final S3StorageUrlProvider s3StorageUrlProvider) {
        return new S3Adapter(documentStorageFileRepository, documentStorageFolderRepository, fileService, s3StorageUrlProvider);
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

    @Bean
    public Consumer<Message<CreateFileDto>> createFile(final StreamingAdapter streamingAdapter) {
        return streamingAdapter.createFile();
    }

    @Bean
    public Consumer<Message<CreateProcedureDto>> createProcedure(final StreamingAdapter streamingAdapter) {
        return streamingAdapter.createProcedure();
    }

    @Bean
    public Consumer<Message<CreateDocumentDto>> createDocument(final StreamingAdapter streamingAdapter) {
        return streamingAdapter.createDocument();
    }

    @Bean
    public Consumer<Message<UpdateDocumentDto>> updateDocument(final StreamingAdapter streamingAdapter) {
        return streamingAdapter.updateDocument();
    }

    @Bean
    public Consumer<Message<DepositObjectDto>> depositObject(final StreamingAdapter streamingAdapter) {
        return streamingAdapter.depositObject();
    }

    @Bean
    public Consumer<Message<CancelObjectDto>> cancelObject(final StreamingAdapter streamingAdapter) {
        return streamingAdapter.cancelObject();
    }

    @Bean
    public Consumer<Message<ReadContentDto>> readContent(final StreamingAdapter streamingAdapter) {
        return streamingAdapter.readContent();
    }

    @Bean
    public Consumer<Message<SearchObjectDto>> searchFile(final StreamingAdapter streamingAdapter) {
        return streamingAdapter.searchFile();
    }

    @Bean
    public Consumer<Message<SearchObjectDto>> searchSubjectArea(final StreamingAdapter streamingAdapter) {
        return streamingAdapter.searchSubjectArea();
    }

    @Bean
    @ConditionalOnMissingBean
    public StreamingAdapter streamingAdapter(
            final ProcessApi processApi,
            final ErrorApi errorApi,
            final CreateFileInPort createFileInPort,
            final CreateProcedureInPort createProcedureInPort,
            final CreateDocumentInPort createDocumentInPort,
            final UpdateDocumentInPort updateDocumentInPort,
            final DepositObjectInPort depositObjectInPort,
            final CancelObjectInPort cancelObjectInPort,
            final ReadContentInPort readContentInPort,
            final SearchFileInPort searchFileInPort,
            final SearchSubjectAreaInPort searchSubjectAreaInPort
    ) {
        return new StreamingAdapter(
                processApi,
                errorApi,
                createFileInPort,
                createProcedureInPort,
                createDocumentInPort,
                updateDocumentInPort,
                depositObjectInPort,
                cancelObjectInPort,
                readContentInPort,
                searchFileInPort,
                searchSubjectAreaInPort);
    }

}
