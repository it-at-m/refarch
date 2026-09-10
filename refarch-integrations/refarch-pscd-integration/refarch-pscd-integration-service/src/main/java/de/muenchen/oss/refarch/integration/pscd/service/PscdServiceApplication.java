package de.muenchen.oss.refarch.integration.pscd.service;

import de.muenchen.oss.refarch.integration.pscd.service.configuration.PscdInboundProperties;
import de.muenchen.oss.refarch.integration.pscd.service.configuration.PscdNotificationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.EventListener;

/**
 * Standalone PSCD service.
 *
 * <p>
 * Uses the PSCD starter as a library and adds the inbound channels for the refarch-owned canonical
 * contract: a SOAP endpoint and a REST endpoint (both on by default), plus an optional flat-file
 * poller, each toggled independently via {@code refarch.pscd.inbound.*}. All channels map their
 * payload onto the domain and feed the same core dispatch entry.
 * </p>
 */
@SpringBootApplication
@EnableConfigurationProperties({ PscdInboundProperties.class, PscdNotificationProperties.class })
@RequiredArgsConstructor
@Slf4j
public class PscdServiceApplication {

    private final PscdInboundProperties inboundProperties;

    public static void main(final String[] args) {
        SpringApplication.run(PscdServiceApplication.class, args);
    }

    /**
     * Report the effective inbound configuration once the context is up. Which channels are live is the
     * first thing to establish when a batch is missing, and it is not otherwise visible in the log.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void logInboundConfiguration() {
        final PscdInboundProperties.FileProperties file = this.inboundProperties.getFile();
        log.info("PSCD inbound channels: soap={}, rest={}, file={} (directory='{}', pollInterval={} ms)",
                this.inboundProperties.getSoap().isEnabled(), this.inboundProperties.getRest().isEnabled(),
                file.isEnabled(), file.getDirectory(), file.getPollInterval());
    }

}
