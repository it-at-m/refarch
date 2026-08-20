package de.muenchen.oss.refarch.integration.pscd.service.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata for the inbound REST channel.
 *
 * <p>
 * The specification itself is generated at runtime by springdoc from
 * {@code PscdInboundRestController} and the contract-generated canonical model (served at
 * {@code /v3/api-docs}, UI at {@code /swagger-ui.html}). It is the REST counterpart to the WSDL
 * served for the SOAP edge, and, like it, transitively derived from {@code pscd-canonical.xsd}.
 * This class only supplies the document's title/version/description; the batch operation is
 * documented only while the REST channel is enabled, since the controller it is inferred from is
 * itself conditional.
 * </p>
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "PSCD Inbound API",
                version = "v1",
                description = "refarch-owned canonical PSCD \"Satzarten\" batch contract, served as JSON. The same "
                        + "canonical model backs the SOAP edge (see the published WSDL)."
        )
)
public class PscdOpenApiConfiguration {
}
