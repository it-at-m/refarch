package de.muenchen.oss.refarch.integration.pscd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import de.muenchen.oss.refarch.integration.email.application.port.out.MailOutPort;
import de.muenchen.oss.refarch.integration.email.domain.model.TextMail;
import de.muenchen.oss.refarch.integration.pscd.application.port.out.PscdOutPort;
import de.muenchen.oss.refarch.integration.pscd.domain.exception.PscdProcessingException;
import de.muenchen.oss.refarch.integration.pscd.domain.model.PscdSatzarten;
import de.muenchen.oss.refarch.integration.pscd.service.adapter.in.file.PscdRecordFixtures;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

// Isolate the file channel: only file on, the other two off. Since the SOAP edge moved to Spring WS it
// would also start in this MOCK web environment, so disabling it is now purely about isolation: this
// test asserts that no other channel's beans are present.
@SpringBootTest(
        properties = {
                "refarch.pscd.inbound.file.enabled=true",
                "refarch.pscd.inbound.soap.enabled=false",
                "refarch.pscd.inbound.rest.enabled=false",
                "refarch.pscd.inbound.file.stable-for=0",
                "refarch.pscd.notification.file.enabled=true",
                "refarch.pscd.notification.to=ops@example.org",
                "refarch.pscd.inbound.security.username=pscd-sender",
                "refarch.pscd.inbound.security.password=s3cret"
        }
)
class PscdServiceFileModeTest {

    // Static so @DynamicPropertySource can expose it before the context loads; @TempDir requires it mutable.
    @SuppressWarnings("PMD.MutableStaticState")
    @TempDir
    /* default */ static Path inbox;

    @Autowired
    private ApplicationContext applicationContext;

    @MockitoBean
    private PscdOutPort pscdOutPort;

    @MockitoBean
    private MailOutPort mailOutPort;

    @DynamicPropertySource
    /* default */ static void inboxProperty(final DynamicPropertyRegistry registry) {
        registry.add("refarch.pscd.inbound.file.directory", inbox::toString);
    }

    @Test
    void activatesFilePollerAndNotSoapEndpoint() {
        assertThat(this.applicationContext.containsBean("pscdFilePoller")).isTrue();
        assertThat(this.applicationContext.containsBean("pscdSoapInboundEndpoint")).isFalse();
    }

    @Test
    void processedFileIsHandedToTheOutPortAndMovedToDone() throws Exception {
        Files.writeString(inbox.resolve("success.txt"),
                PscdRecordFixtures.line("010", Map.of("ABSTIMMSUMME", "1234", "VORZEICHEN", "+"))
                        + '\n'
                        + PscdRecordFixtures.validLine("200", Map.of("PSOBKEY", "OBJ1", "EINNAHMEART", "EA", "BETRW", "100")));

        final ArgumentCaptor<PscdSatzarten> captor = ArgumentCaptor.forClass(PscdSatzarten.class);
        verify(this.pscdOutPort, timeout(10_000)).send(captor.capture());
        assertThat(captor.getValue().getFilename()).isEqualTo("success.txt");
        assertThat(captor.getValue().getSatzart010()).isNotNull();
        assertThat(captor.getValue().getSatzart200()).hasSize(1);
        await().atMost(Duration.ofSeconds(10)).until(() -> moved(".done", "success"));
    }

    @Test
    void failedFileIsMovedToErrorAndNotReprocessed() throws Exception {
        doThrow(new PscdProcessingException("endpoint down", new RuntimeException()))
                .when(this.pscdOutPort).send(any());

        Files.writeString(inbox.resolve("failure.txt"), PscdRecordFixtures.line("010", Map.of("ABSTIMMSUMME", "1", "VORZEICHEN", "+")));

        await().atMost(Duration.ofSeconds(10)).until(() -> moved(".error", "failure"));
        verify(this.pscdOutPort, timeout(2_000).times(1)).send(any());

        // End to end through the wiring: the mail names the batch and where it was filed, from the
        // template shipped with the service.
        final ArgumentCaptor<TextMail> captor = ArgumentCaptor.forClass(TextMail.class);
        verify(this.mailOutPort, timeout(10_000)).sendTextMail(captor.capture());
        final TextMail mail = captor.getValue();
        assertThat(mail.getReceivers()).isEqualTo("ops@example.org");
        assertThat(mail.getBody())
                .contains("Channel:  file")
                .contains("Batch:    failure.txt")
                .containsPattern("Location: \\.error/failure_\\d{8}_\\d{9}\\.txt")
                .contains("endpoint down");
    }

    /**
     * The poller decodes with the configured charset, which defaults to the host export's
     * ISO-8859-1. This is what the setting exists for: an umlaut written as one ISO-8859-1 byte would
     * abort the whole file if it were decoded as UTF-8, and the surrounding columns must still line
     * up once it is decoded correctly.
     */
    @Test
    void decodesIso88591UmlautsAndKeepsTheFollowingColumnsAligned() throws Exception {
        final String record = PscdRecordFixtures.line("100",
                Map.of("PSOBKEY", "OBJ1", "PARTNER", "PA1", "PSOBTXTB1", "Grundstück Äußere", "FACHDST_SB", "Müller"));
        Files.write(inbox.resolve("umlaut.txt"), record.getBytes(StandardCharsets.ISO_8859_1));

        final ArgumentCaptor<PscdSatzarten> captor = ArgumentCaptor.forClass(PscdSatzarten.class);
        verify(this.pscdOutPort, timeout(10_000)).send(captor.capture());
        assertThat(captor.getValue().getSatzart100()).singleElement()
                .satisfies(satzart -> {
                    assertThat(satzart.getPsobtxtb1()).isEqualTo("Grundstück Äußere");
                    // A column after the umlauts: proof that nothing shifted while decoding.
                    assertThat(satzart.getFachdstSb()).isEqualTo("Müller");
                });
    }

    /**
     * A file whose columns do not line up is delivered rather than held back: the line that could not
     * be mapped travels as an error record, so the batch counts as processed and is filed as done. The
     * deliberate trade-off described in {@code PscdSatzartenParser}, asserted end to end.
     */
    @Test
    void misalignedFileIsDeliveredWithAnErrorRecordAndFiledAsDone() throws Exception {
        Files.writeString(inbox.resolve("misaligned.txt"), "this is not a fixed-width PSCD record");

        final ArgumentCaptor<PscdSatzarten> captor = ArgumentCaptor.forClass(PscdSatzarten.class);
        verify(this.pscdOutPort, timeout(10_000).atLeastOnce()).send(captor.capture());
        assertThat(captor.getAllValues())
                .anySatisfy(batch -> assertThat(batch.getSatzartFehler()).isNotEmpty());
        await().atMost(Duration.ofSeconds(10)).until(() -> moved(".done", "misaligned"));
        assertThat(moved(".error", "misaligned")).isFalse();
    }

    /**
     * Whether the batch of the given base name has been filed into the subdirectory. Matched by
     * pattern rather than by name because the poller stamps the move time before the extension.
     */
    private static boolean moved(final String subdir, final String base) throws Exception {
        final Path target = inbox.resolve(subdir);
        if (!Files.isDirectory(target)) {
            return false;
        }
        try (Stream<Path> files = Files.list(target)) {
            return files.anyMatch(file -> file.getFileName().toString().matches(base + "_\\d{8}_\\d{9}\\.txt"));
        }
    }
}
