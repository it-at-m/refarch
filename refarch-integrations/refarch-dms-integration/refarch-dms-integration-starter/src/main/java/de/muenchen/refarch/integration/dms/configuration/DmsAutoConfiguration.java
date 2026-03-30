package de.muenchen.refarch.integration.dms.configuration;

import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.LHMBAI151700GIWSDSoap;
import de.muenchen.refarch.integration.dms.adapter.out.fabasoft.FabasoftClientConfiguration;
import de.muenchen.refarch.integration.dms.adapter.out.fabasoft.FabasoftOutAdapter;
import de.muenchen.refarch.integration.dms.adapter.out.fabasoft.FabasoftProperties;
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
public class DmsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FabasoftOutAdapter fabasoftAdapter(final FabasoftProperties dmsProperties, final LHMBAI151700GIWSDSoap wsClient) {
        return new FabasoftOutAdapter(dmsProperties, wsClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public LHMBAI151700GIWSDSoap wsClient(final FabasoftClientConfiguration fabasoftClientConfiguration) {
        return fabasoftClientConfiguration.dmsWsClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public CancelObjectOutPort cancelObjectOutPort(final FabasoftOutAdapter fabasoftOutAdapter) {
        return fabasoftOutAdapter;
    }

    @Bean
    @ConditionalOnMissingBean
    public CreateDocumentOutPort createDocumentOutPort(final FabasoftOutAdapter fabasoftOutAdapter) {
        return fabasoftOutAdapter;
    }

    @Bean
    @ConditionalOnMissingBean
    public CreateFileOutPort createFileOutPort(final FabasoftOutAdapter fabasoftOutAdapter) {
        return fabasoftOutAdapter;
    }

    @Bean
    @ConditionalOnMissingBean
    public CreateProcedureOutPort createProcedureOutPort(final FabasoftOutAdapter fabasoftOutAdapter) {
        return fabasoftOutAdapter;
    }

    @Bean
    @ConditionalOnMissingBean
    public DepositObjectOutPort depositObjectOutPort(final FabasoftOutAdapter fabasoftOutAdapter) {
        return fabasoftOutAdapter;
    }

    @Bean
    @ConditionalOnMissingBean
    public ListContentOutPort listContentOutPort(final FabasoftOutAdapter fabasoftOutAdapter) {
        return fabasoftOutAdapter;
    }

    @Bean
    @ConditionalOnMissingBean
    public ReadContentOutPort readContentOutPort(final FabasoftOutAdapter fabasoftOutAdapter) {
        return fabasoftOutAdapter;
    }

    @Bean
    @ConditionalOnMissingBean
    public ReadMetadataOutPort readMetadataOutPort(final FabasoftOutAdapter fabasoftOutAdapter) {
        return fabasoftOutAdapter;
    }

    @Bean
    @ConditionalOnMissingBean
    public SearchFileOutPort searchFileOutPort(final FabasoftOutAdapter fabasoftOutAdapter) {
        return fabasoftOutAdapter;
    }

    @Bean
    @ConditionalOnMissingBean
    public SearchSubjectAreaOutPort searchSubjectAreaOutPort(final FabasoftOutAdapter fabasoftOutAdapter) {
        return fabasoftOutAdapter;
    }

    @Bean
    @ConditionalOnMissingBean
    public UpdateDocumentOutPort updateDocumentOutPort(final FabasoftOutAdapter fabasoftOutAdapter) {
        return fabasoftOutAdapter;
    }
}
