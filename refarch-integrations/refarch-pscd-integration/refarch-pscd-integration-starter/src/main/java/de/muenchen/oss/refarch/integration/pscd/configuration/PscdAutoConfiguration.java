package de.muenchen.oss.refarch.integration.pscd.configuration;

import de.muenchen.oss.refarch.integration.pscd.adapter.out.pscd.PscdOutAdapter;
import de.muenchen.oss.refarch.integration.pscd.application.port.in.SubmitPscdBatchInPort;
import de.muenchen.oss.refarch.integration.pscd.application.port.out.PscdOutPort;
import de.muenchen.oss.refarch.integration.pscd.application.service.SubmitPscdBatchService;
import de.muenchen.oss.refarch.integration.pscd.client.PscdSoapClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration wiring the PSCD delivery pipeline into any Spring Boot application.
 *
 * <p>
 * Exposes three overridable beans: the SOAP client, the outbound {@code pscdOutPort} that delivers
 * a domain batch to the PSCD endpoint, and the inbound port, a plain application service that
 * hands submitted batches straight to the out-port. The core pipeline is plain Java + Spring; the
 * inbound transport adapters (SOAP, file) live in the service module.
 * </p>
 */
@Configuration
@RequiredArgsConstructor
@ComponentScan(basePackages = { "de.muenchen.oss.refarch.integration.pscd" })
@EnableConfigurationProperties({ PscdProperties.class })
public class PscdAutoConfiguration {

    private final PscdProperties pscdProperties;

    @Bean
    @ConditionalOnMissingBean
    public PscdSoapClient pscdSoapClient() {
        final PscdProperties.Client client = this.pscdProperties.getClient();
        return new PscdSoapClient(client.getUrl(), client.getUsername(), client.getPassword());
    }

    @Bean
    @ConditionalOnMissingBean
    public PscdOutPort pscdOutPort(final PscdSoapClient pscdSoapClient) {
        return new PscdOutAdapter(pscdSoapClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public SubmitPscdBatchInPort submitPscdBatchInPort(final PscdOutPort pscdOutPort) {
        return new SubmitPscdBatchService(pscdOutPort);
    }
}
