package de.muenchen.oss.refarch.integration.pscd.service.adapter.in.soap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.Wsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

/**
 * Publishes the refarch-owned canonical inbound contract for the SOAP edge.
 */
@Configuration
@ConditionalOnProperty(name = "refarch.pscd.inbound.soap.enabled", havingValue = "true", matchIfMissing = true)
public class PscdSoapInboundConfiguration {

    /** Bean name is the URL: {@code /ws/pscd/pscdInbound.wsdl}. */
    @Bean
    public Wsdl11Definition pscdInbound() {
        return new SimpleWsdl11Definition(new ClassPathResource("wsdl/pscd-inbound.wsdl"));
    }

    /** Bean name is the URL: {@code /ws/pscd/pscdCanonical.xsd}. */
    @Bean
    public XsdSchema pscdCanonical() {
        return new SimpleXsdSchema(new ClassPathResource("xsd/pscd-canonical.xsd"));
    }
}
