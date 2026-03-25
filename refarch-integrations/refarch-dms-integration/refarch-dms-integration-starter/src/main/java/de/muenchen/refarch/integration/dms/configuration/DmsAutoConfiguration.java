package de.muenchen.refarch.integration.dms.configuration;

import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.LHMBAI151700GIWSDSoap;
import de.muenchen.refarch.integration.dms.adapter.out.fabasoft.FabasoftAdapter;
import de.muenchen.refarch.integration.dms.adapter.out.fabasoft.FabasoftClientConfiguration;
import de.muenchen.refarch.integration.dms.adapter.out.fabasoft.FabasoftProperties;
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
    public FabasoftAdapter fabasoftAdapter(final FabasoftProperties dmsProperties, final LHMBAI151700GIWSDSoap wsClient) {
        return new FabasoftAdapter(dmsProperties, wsClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public LHMBAI151700GIWSDSoap wsClient(final FabasoftClientConfiguration fabasoftClientConfiguration) {
        return fabasoftClientConfiguration.dmsWsClient();
    }
}
