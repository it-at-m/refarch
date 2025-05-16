package de.muenchen.refarch.integration.dms.configuration;

import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.LHMBAI151700GIWSDSoap;
import de.muenchen.refarch.integration.dms.adapter.out.fabasoft.FabasoftAdapter;
import de.muenchen.refarch.integration.dms.adapter.out.fabasoft.FabasoftClientConfiguration;
import de.muenchen.refarch.integration.dms.adapter.out.fabasoft.FabasoftProperties;
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
import de.muenchen.refarch.integration.dms.application.port.out.ReadContentOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.ReadMetadataOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.SearchFileOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.SearchSubjectAreaOutPort;
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

@Configuration
@RequiredArgsConstructor
@Import(FabasoftClientConfiguration.class)
@EnableConfigurationProperties({ FabasoftProperties.class })
@SuppressWarnings("PMD.CouplingBetweenObjects")
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
            final ListContentOutPort listContentOutPort) {
        return new CreateDocumentUseCase(createDocumentOutPort, listContentOutPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public UpdateDocumentInPort updateDocumentInPort(
            final UpdateDocumentOutPort updateDocumentOutPort,
            final ListContentOutPort listContentOutPort) {
        return new UpdateDocumentUseCase(updateDocumentOutPort, listContentOutPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public DepositObjectInPort depositObjectInPort(final DepositObjectOutPort depositObjectOutPort) {
        return new DepositObjectUseCase(depositObjectOutPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public CancelObjectInPort cancelObjectInPort(final CancelObjectOutPort cancelObjectOutPort) {
        return new CancelObjectUseCase(cancelObjectOutPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReadContentInPort readContentInPort(final ReadContentOutPort readContentOutPort) {
        return new ReadContentUseCase(readContentOutPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public SearchFileInPort searchFileInPort(final SearchFileOutPort searchFileOutPort) {
        return new SearchFileUseCase(searchFileOutPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public SearchSubjectAreaInPort searchSubjectAreaInPort(final SearchSubjectAreaOutPort searchSubjectAreaOutPort) {
        return new SearchSubjectAreaUseCase(searchSubjectAreaOutPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReadMetadataInPort readMetadataInPort(final ReadMetadataOutPort readMetadataOutPort) {
        return new ReadMetadataUseCase(readMetadataOutPort);
    }
}
