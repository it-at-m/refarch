package de.muenchen.oss.refarch.integration.pscd.service.configuration;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Inbound-channel configuration for the standalone service ({@code refarch.pscd.inbound}).
 *
 * <p>
 * The three channels are independent; any combination may run at once. Enablement is also read
 * directly via {@code @ConditionalOnProperty} on each channel's configuration (so the beans are
 * only created when enabled); this class provides the typed surface, defaults and IDE metadata, and
 * is the source of the file poller's directory. Inbound concerns live in the service (not the
 * starter) so library consumers never accidentally start a server or poller.
 * </p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "refarch.pscd.inbound")
public class PscdInboundProperties {

    // The field names, not the holder type names, determine the keys:
    // refarch.pscd.inbound.{soap,rest,file,security}.*
    @NestedConfigurationProperty
    private SoapProperties soap = new SoapProperties();

    @NestedConfigurationProperty
    private RestProperties rest = new RestProperties();

    @NestedConfigurationProperty
    private FileProperties file = new FileProperties();

    @NestedConfigurationProperty
    private SecurityProperties security = new SecurityProperties();

    /**
     * HTTP Basic in front of both HTTP channels: one account, the same for SOAP and REST, and always
     * required. The file channel has no HTTP surface and is unaffected.
     */
    @Getter
    @Setter
    public static class SecurityProperties {

        /**
         * Username the sending systems authenticate with. Mandatory: there is no default, and the
         * service refuses to start without it rather than inventing or shipping a credential.
         */
        private String username;

        /**
         * Password for {@link #username}. Mandatory, like the username.
         *
         * <p>
         * Supply it from the environment or a secret ({@code REFARCH_PSCD_INBOUND_SECURITY_PASSWORD}),
         * not from a committed file. Taken as plain text unless it carries a Spring Security encoder
         * prefix. {@code {bcrypt}$2a$10$…} is stored and compared as a hash, anything without a
         * {@code {…}} prefix is treated as the literal password.
         * </p>
         */
        private String password;
    }

    @Getter
    @Setter
    public static class SoapProperties {
        /**
         * Host the inbound SOAP endpoint (canonical contract), served at {@code /ws/pscd}.
         */
        private boolean enabled = true;
    }

    @Getter
    @Setter
    public static class RestProperties {
        /**
         * Expose the inbound REST endpoint (canonical contract as JSON) at {@code POST /api/pscd/batches}.
         */
        private boolean enabled = true;
    }

    @Getter
    @Setter
    public static class FileProperties {
        /** Poll a directory for flat-file batches. Off by default. */
        private boolean enabled = false;
        /** Directory polled for flat-file batches when {@link #enabled} is true. */
        private String directory = "./pscd-inbox";
        /**
         * How long a file's size and modification time must stay unchanged before it is taken up, so a
         * batch still being written is not read half-finished.
         */
        private Duration stableFor = Duration.ofSeconds(2);
        /**
         * Subdirectory of {@link #directory} a batch is moved to for the duration of its processing,
         * before it is filed as done or error. Created on demand, and never polled.
         */
        private String workingDirectory = ".working";
        /**
         * Subdirectory of {@link #directory} a batch is moved to once it has been delivered. Created on
         * demand, and never polled: the poll only picks up regular files directly in
         * {@link #directory}.
         */
        private String doneDirectory = ".done";
        /**
         * Subdirectory of {@link #directory} a batch whose processing failed is moved to, so it is not
         * re-polled forever. Same rules as {@link #doneDirectory}.
         */
        private String errorDirectory = ".error";
        /** Delay in milliseconds between the end of one poll and the start of the next. */
        private long pollInterval = 1000L;
        /**
         * Charset the polled batch files are decoded with, defaulting to the host export's
         * {@code ISO-8859-1}.
         *
         * <p>
         * Not cosmetic: the records are fixed-width, so column offsets are character offsets and only
         * line up if the file is decoded the way it was written. Decoding an ISO-8859-1 export as
         * UTF-8 fails outright on the first umlaut in {@code PSOBTXTB*}, {@code FACHDST_SB} or
         * {@code KD_KENN}; decoding a UTF-8 file as ISO-8859-1 silently turns each umlaut into two
         * characters and shifts every column after it. Set it to whatever the sending side writes.
         * </p>
         */
        private Charset charset = StandardCharsets.ISO_8859_1;
    }
}
