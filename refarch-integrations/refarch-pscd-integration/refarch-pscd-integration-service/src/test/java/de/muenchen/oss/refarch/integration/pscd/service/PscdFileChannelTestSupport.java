package de.muenchen.oss.refarch.integration.pscd.service;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import de.lhm.pi.pscd.rahmenschnittstelle.DTSOAPSatzarten;
import de.muenchen.oss.refarch.integration.pscd.client.PscdSoapClient;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.ClassPathResource;

/**
 * What a test driving the file channel with one of the real sample batches needs: getting the
 * sample
 * into the polled directory, finding it again in an archive, and reading the accounting trails.
 *
 * <p>
 * Shared rather than copied per test, because the two interesting parts, that the samples are used
 * <em>unmodified</em> and that the trails are appended to across a build so only what a run added
 * can be asserted, are easy to get subtly wrong twice.
 * </p>
 */
public final class PscdFileChannelTestSupport {

    /** Where the accounting trails land, as {@code logback-spring.xml} configures them. */
    public static final Path ACCOUNT_ERROR_LOG = Path.of("logs/account-error.log");
    public static final Path COMPLETION_LOG = Path.of("logs/completion.log");

    private PscdFileChannelTestSupport() {
    }

    /**
     * One run of a sample batch through the file channel.
     *
     * @param request what reached the PSCD SOAP client
     * @param accountErrorsBefore how many error-trail lines this batch had before the run
     * @param completionsBefore how many completion lines this batch had before the run
     */
    public record Delivery(DTSOAPSatzarten request, int accountErrorsBefore, int completionsBefore) {
    }

    /**
     * Put a sample in the polled directory and wait for the request it turns into.
     *
     * <p>
     * The trail positions are taken before the file appears, so an assertion can be about what this run
     * added; the wait is on the mocked client rather than on a fixed delay, because the poll is
     * scheduled and its timing is not what a test should depend on.
     * </p>
     *
     * @param inbox the polled directory
     * @param batch the sample's filename
     * @param client the mocked SOAP client the request is captured from
     */
    public static Delivery deliver(final Path inbox, final String batch, final PscdSoapClient client) throws IOException {
        final int accountErrorsBefore = linesFor(ACCOUNT_ERROR_LOG, batch).size();
        final int completionsBefore = linesFor(COMPLETION_LOG, batch).size();
        copySampleIntoInbox(inbox, batch);

        final ArgumentCaptor<DTSOAPSatzarten> captor = ArgumentCaptor.forClass(DTSOAPSatzarten.class);
        verify(client, timeout(15_000)).send(captor.capture());
        return new Delivery(captor.getValue(), accountErrorsBefore, completionsBefore);
    }

    /**
     * Copy a sample batch from {@code src/test/resources/pscd} into the polled directory. A copy, so
     * the original stays exactly as the predecessor produced it, which is the whole value of those
     * files.
     *
     * @param inbox the polled directory
     * @param batch the sample's filename, which is also the batch name it is delivered under
     */
    public static void copySampleIntoInbox(final Path inbox, final String batch) throws IOException {
        try (InputStream sample = new ClassPathResource("pscd/" + batch).getInputStream()) {
            Files.write(inbox.resolve(batch), sample.readAllBytes());
        }
    }

    /**
     * Whether the batch has been filed into the given archive, under the name the poller stamps on it.
     *
     * @param inbox the polled directory the archive is a child of
     * @param archive the archive subdirectory, e.g. {@code archive/successful}
     * @param batch the batch's filename
     */
    public static boolean archived(final Path inbox, final String archive, final String batch) throws IOException {
        final Path target = inbox.resolve(archive);
        if (!Files.isDirectory(target)) {
            return false;
        }
        try (Stream<Path> files = Files.list(target)) {
            return files.anyMatch(file -> file.getFileName().toString().matches(batch + "_\\d{8}_\\d{9}.*"));
        }
    }

    /** The lines of a accounting trail that belong to one batch, whenever they were written. */
    public static List<String> linesFor(final Path log, final String batch) throws IOException {
        if (!Files.exists(log)) {
            return List.of();
        }
        return Files.readAllLines(log, StandardCharsets.UTF_8).stream()
                .filter(line -> line.contains(batch))
                .toList();
    }

    /**
     * What this run appended to a trail for one batch, so a build over an existing log asserts the same
     * thing as one over an empty directory.
     *
     * @param before how many lines {@link #linesFor} returned before the run
     */
    public static List<String> addedTo(final Path log, final String batch, final int before) throws IOException {
        final List<String> lines = linesFor(log, batch);
        return lines.subList(before, lines.size());
    }
}
