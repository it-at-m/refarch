package de.muenchen.oss.refarch.integration.pscd.configuration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the PSCD integration (library concerns: the outbound SOAP client).
 *
 * <p>
 * The inbound-source toggle ({@code isPscdReadFile}) and directory settings are owned by the
 * standalone
 * service module, not the starter, so library consumers never accidentally start a SOAP server /
 * file poller.
 * </p>
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "refarch.pscd")
public class PscdProperties {

    @NotNull @Valid @NestedConfigurationProperty
    private Client client;

    @Getter
    @Setter
    public static class Client {

        /** SOAP endpoint the client calls. */
        @NotBlank private String url;

        /**
         * Username for HTTP Basic against that endpoint. Optional: while it is unset the message is sent
         * without credentials, which is how this integration behaved before the setting existed.
         */
        private String username;

        /**
         * Password for {@link #username}, sent preemptively with every message.
         *
         * <p>
         * HTTP Basic only base64-encodes the credential, so it is readable by anything on the wire
         * unless {@link #url} is {@code https}. Supply it from the environment or a secret
         * ({@code REFARCH_PSCD_CLIENT_PASSWORD}) rather than from a committed configuration file; the
         * name is deliberately {@code password}, which is one of the keys Spring Boot masks in
         * {@code /actuator/env} and {@code /actuator/configprops}.
         * </p>
         */
        private String password;

    }
}
