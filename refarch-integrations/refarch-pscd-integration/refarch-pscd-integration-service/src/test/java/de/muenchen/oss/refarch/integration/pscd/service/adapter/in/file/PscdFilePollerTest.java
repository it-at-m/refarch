package de.muenchen.oss.refarch.integration.pscd.service.adapter.in.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import de.muenchen.oss.refarch.integration.pscd.application.port.in.SubmitPscdBatchInPort;
import de.muenchen.oss.refarch.integration.pscd.domain.exception.PscdProcessingException;
import de.muenchen.oss.refarch.integration.pscd.domain.model.PscdSatzarten;
import de.muenchen.oss.refarch.integration.pscd.service.adapter.in.PscdInboundChannel;
import de.muenchen.oss.refarch.integration.pscd.service.adapter.out.notification.PscdFailureNotifier;
import de.muenchen.oss.refarch.integration.pscd.service.configuration.PscdInboundProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

/**
 * How the poller decides what to pick up, and where it files a batch once it has had its one
 * attempt. Drives {@code poll()} directly rather than through Spring: the scheduling is not what is
 * under test here.
 */
@ExtendWith(OutputCaptureExtension.class)
class PscdFilePollerTest {

    private static final String WORKING = "inflight";
    private static final String DONE = "processed";
    private static final String ERROR = "rejected";

    /** A batch that goes through, as it is named in the inbox. */
    private static final String SUCCESS = "success.txt";

    /** {@code <base>_yyyyMMdd_HHmmssSSS<extension>}, the name a batch is filed under. */
    private static final String STAMPED = "%s_\\d{8}_\\d{9}%s";

    /** The extension half of {@link #STAMPED}, as a regex. */
    private static final String TXT = "\\.txt";

    /**
     * Settle time for the tests that exercise the check. Short enough to keep them quick, long enough
     * that the sleeps around it are not racing the filesystem.
     */
    private static final long SETTLE_MILLIS = 100L;

    @TempDir
    private Path inbox;

    private final SubmitPscdBatchInPort inPort = mock(SubmitPscdBatchInPort.class);
    private final PscdFailureNotifier failureNotifier = mock(PscdFailureNotifier.class);

    @AfterEach
    void restorePermissions() throws IOException {
        // The two tests that block a directory leave it read-only; @TempDir cannot clean up around that.
        makeWritable(this.inbox);
    }

    @Test
    void movesADeliveredBatchIntoTheConfiguredDoneDirectoryUnderItsStampedName() throws IOException {
        writeBatch(SUCCESS);

        poller().poll();

        verify(this.inPort).submit(any());
        assertThat(namesIn(DONE)).singleElement().matches(name -> name.matches(STAMPED.formatted("success", TXT)));
        assertThat(this.inbox.resolve(SUCCESS)).doesNotExist();
        assertThat(namesIn(WORKING)).isEmpty();
    }

    @Test
    void movesAFailedBatchIntoTheConfiguredErrorDirectoryUnderItsStampedName() throws IOException {
        doThrow(new PscdProcessingException("endpoint down", new RuntimeException())).when(this.inPort).submit(any());
        writeBatch("failure.txt");

        poller().poll();

        assertThat(namesIn(ERROR)).singleElement().matches(name -> name.matches(STAMPED.formatted("failure", TXT)));
        assertThat(namesIn(DONE)).isEmpty();
    }

    /** The notification names where the batch ended up, so it has to be sent after the move. */
    @Test
    void notifiesAboutAFailedBatchWithTheNameItWasFiledUnder() throws IOException {
        doThrow(new PscdProcessingException("endpoint down", new RuntimeException())).when(this.inPort).submit(any());
        writeBatch("failure.txt");

        poller().poll();

        final ArgumentCaptor<String> location = ArgumentCaptor.forClass(String.class);
        verify(this.failureNotifier).notifyFailure(eq(PscdInboundChannel.FILE), eq("failure.txt"), location.capture(),
                any(PscdProcessingException.class));
        assertThat(location.getValue()).isEqualTo(ERROR + '/' + namesIn(ERROR).get(0));
    }

    @Test
    void doesNotNotifyAboutADeliveredBatch() throws IOException {
        writeBatch(SUCCESS);

        poller().poll();

        verifyNoInteractions(this.failureNotifier);
    }

    /**
     * A line the parser cannot map no longer costs the batch: it travels as an error record, so the
     * file counts as processed and is filed as done rather than diverted.
     */
    @Test
    void deliversABatchWhoseLinesCouldNotBeMappedAndFilesItAsDone() throws IOException {
        Files.writeString(this.inbox.resolve("misaligned.txt"), "this is not a fixed-width PSCD record");

        poller().poll();

        final ArgumentCaptor<PscdSatzarten> captor = ArgumentCaptor.forClass(PscdSatzarten.class);
        verify(this.inPort).submit(captor.capture());
        assertThat(captor.getValue().getSatzartFehler()).isNotEmpty();
        assertThat(namesIn(DONE)).singleElement().matches(name -> name.matches(STAMPED.formatted("misaligned", TXT)));
        assertThat(namesIn(ERROR)).isEmpty();
    }

    /** Nothing to insert the stamp before, so it goes at the end rather than into the name. */
    @Test
    void appendsTheStampToABatchWithoutAnExtension() throws IOException {
        writeBatch("buchungssaetze");

        poller().poll();

        assertThat(namesIn(DONE)).singleElement().matches(name -> name.matches(STAMPED.formatted("buchungssaetze", "")));
    }

    /** The subdirectories are not polled, so a filed batch is never picked up a second time. */
    @Test
    void doesNotPollItsOwnSubdirectories() throws IOException {
        writeBatch(SUCCESS);
        final PscdFilePoller poller = poller();

        poller.poll();
        poller.poll();

        verify(this.inPort).submit(any());
        assertThat(namesIn(DONE)).hasSize(1);
    }

    /**
     * The batch is taken up, moved out of the polled directory, before anything is read or
     * delivered, which is what stops a file that cannot be filed afterwards from being delivered over
     * and over.
     */
    @Nested
    class TakingUp {

        /**
         * The case this ordering exists for. Delivery succeeds, filing it as done does not, and the
         * batch must still reach PSCD exactly once however many polls follow, since a second delivery
         * would be a duplicate financial posting.
         */
        @Test
        void deliversOnlyOnceWhenTheBatchCannotBeFiledAsDone() throws IOException {
            makeUnwritable(Files.createDirectories(PscdFilePollerTest.this.inbox.resolve(DONE)));
            writeBatch(SUCCESS);
            final PscdFilePoller poller = poller();

            poller.poll();
            poller.poll();
            poller.poll();

            verify(PscdFilePollerTest.this.inPort, times(1)).submit(any());
            assertThat(PscdFilePollerTest.this.inbox.resolve(SUCCESS)).doesNotExist();
            assertThat(namesIn(WORKING)).singleElement().matches(name -> name.matches(STAMPED.formatted("success", TXT)));
        }

        /** And the operator is told where it is stuck, rather than that it will come round again. */
        @Test
        void saysWhereAnUnfilableDeliveredBatchIsHeld(final CapturedOutput output) throws IOException {
            makeUnwritable(Files.createDirectories(PscdFilePollerTest.this.inbox.resolve(DONE)));
            writeBatch(SUCCESS);

            poller().poll();

            assertThat(output.getAll())
                    .contains("will NOT be delivered again")
                    .contains("held in '" + WORKING + '/');
        }

        /**
         * If the take-up itself fails nothing has been read, so nothing may be delivered, and the file
         * stays where it is, because the next poll retrying it is harmless precisely because nothing
         * happened.
         */
        @Test
        void deliversNothingWhenTheBatchCannotBeTakenUp(final CapturedOutput output) throws IOException {
            writeBatch(SUCCESS);
            makeUnwritable(PscdFilePollerTest.this.inbox);
            final PscdFilePoller poller = poller();

            poller.poll();
            poller.poll();

            verifyNoInteractions(PscdFilePollerTest.this.inPort);
            assertThat(PscdFilePollerTest.this.inbox.resolve(SUCCESS)).exists();
            // Reported once for the file, not once per poll: at a one-second interval the second form
            // would bury the log.
            assertThat(output.getAll()).containsOnlyOnce("Could not take up PSCD batch 'success.txt'");
        }

        /**
         * A batch left behind by an earlier run may already be at PSCD, so it is reported and left
         * alone rather than picked up again on a guess.
         */
        @Test
        void reportsButDoesNotRetryBatchesHeldFromAnEarlierRun(final CapturedOutput output) throws IOException {
            final Path working = Files.createDirectories(PscdFilePollerTest.this.inbox.resolve(WORKING));
            Files.writeString(working.resolve("stranded_20260804_161500123.txt"), "irrelevant");
            final PscdFilePoller poller = poller();

            poller.reportHeldBatches();
            poller.poll();

            assertThat(output.getAll())
                    .contains("holds 1 batch(es) from an earlier run")
                    .contains("stranded_20260804_161500123.txt");
            verifyNoInteractions(PscdFilePollerTest.this.inPort);
            assertThat(namesIn(WORKING)).hasSize(1);
        }

        @Test
        void staysQuietWhenNothingWasHeldFromAnEarlierRun(final CapturedOutput output) {
            poller().reportHeldBatches();

            assertThat(output.getAll()).doesNotContain("from an earlier run");
        }
    }

    /**
     * A batch is only read once it has stopped changing, so a file still being written is not picked
     * up half-finished, which the parser would repair rather than reject, and so deliver.
     */
    // Sleeping is the point: the behaviour under test is what happens as wall-clock time passes with a
    // file sitting in the inbox, and there is no clock to advance instead, because the poller reads
    // System.nanoTime() so that no configuration can make its own bookkeeping run backwards.
    @SuppressWarnings("PMD.DoNotUseThreads")
    @Nested
    class SettlingDown {

        @Test
        void doesNotTakeUpAFileThatIsStillBeingWritten() throws Exception {
            final Path batch = PscdFilePollerTest.this.inbox.resolve(SUCCESS);
            Files.writeString(batch, PscdRecordFixtures.line("010", Map.of("ABSTIMMSUMME", "1234", "VORZEICHEN", "+")));
            final PscdFilePoller poller = poller(Duration.ofMillis(SETTLE_MILLIS));

            // One look says nothing about whether the write has finished, however long ago it started.
            poller.poll();
            verifyNoInteractions(PscdFilePollerTest.this.inPort);

            Files.writeString(batch, System.lineSeparator() + PscdRecordFixtures.validLine("200",
                    Map.of("PSOBKEY", "OBJ1", "EINNAHMEART", "EA", "BETRW", "100")), StandardOpenOption.APPEND);
            Thread.sleep(SETTLE_MILLIS * 2);

            // Grown since the last look, so the settle clock starts again rather than expiring.
            poller.poll();
            verifyNoInteractions(PscdFilePollerTest.this.inPort);

            Thread.sleep(SETTLE_MILLIS * 2);
            poller.poll();

            final ArgumentCaptor<PscdSatzarten> captor = ArgumentCaptor.forClass(PscdSatzarten.class);
            verify(PscdFilePollerTest.this.inPort).submit(captor.capture());
            // The whole file, not the half that existed at the first poll.
            assertThat(captor.getValue().getSatzart200()).hasSize(1);
        }

        @Test
        void takesUpAFileOnceItHasHeldStillForTheConfiguredTime() throws Exception {
            writeBatch(SUCCESS);
            final PscdFilePoller poller = poller(Duration.ofMillis(SETTLE_MILLIS));

            poller.poll();
            verifyNoInteractions(PscdFilePollerTest.this.inPort);

            Thread.sleep(SETTLE_MILLIS * 2);
            poller.poll();

            verify(PscdFilePollerTest.this.inPort).submit(any());
            assertThat(namesIn(DONE)).hasSize(1);
        }

        /**
         * Switched off, a file is taken up the first time it is seen: the setting for a sending side
         * that renames a finished file into place, and what every other test here relies on.
         */
        @Test
        void takesUpAFileOnSightWhenTheCheckIsOff() throws IOException {
            writeBatch(SUCCESS);

            poller(Duration.ZERO).poll();

            verify(PscdFilePollerTest.this.inPort).submit(any());
        }

        /** Otherwise the bookkeeping would grow by one entry per batch for the life of the service. */
        @Test
        void forgetsAFileThatHasLeftTheInbox() throws IOException {
            writeBatch(SUCCESS);
            final PscdFilePoller poller = poller(Duration.ofMillis(SETTLE_MILLIS));

            poller.poll();
            assertThat(poller.trackedFiles()).isEqualTo(1);

            Files.delete(PscdFilePollerTest.this.inbox.resolve(SUCCESS));
            poller.poll();

            assertThat(poller.trackedFiles()).isZero();
        }
    }

    /** A poller with the settle check off, which is what the tests about filing want. */
    private PscdFilePoller poller() {
        return poller(Duration.ZERO);
    }

    private PscdFilePoller poller(final Duration stableFor) {
        final PscdInboundProperties properties = new PscdInboundProperties();
        properties.getFile().setDirectory(this.inbox.toString());
        properties.getFile().setWorkingDirectory(WORKING);
        properties.getFile().setDoneDirectory(DONE);
        properties.getFile().setErrorDirectory(ERROR);
        properties.getFile().setStableFor(stableFor);
        return new PscdFilePoller(this.inPort, this.failureNotifier, properties);
    }

    private void writeBatch(final String filename) throws IOException {
        Files.writeString(this.inbox.resolve(filename),
                PscdRecordFixtures.line("010", Map.of("ABSTIMMSUMME", "1234", "VORZEICHEN", "+")));
    }

    private List<String> namesIn(final String subdir) throws IOException {
        final Path target = this.inbox.resolve(subdir);
        if (!Files.isDirectory(target)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(target)) {
            return files.map(file -> file.getFileName().toString()).toList();
        }
    }

    /**
     * Take write permission off a directory, and confirm that actually bites before the test goes on:
     * as root it would not, and a test that cannot create the failure it is about must skip rather than
     * pass for the wrong reason.
     */
    private static void makeUnwritable(final Path directory) throws IOException {
        Files.setPosixFilePermissions(directory, PosixFilePermissions.fromString("r-xr-xr-x"));
        boolean blocked = false;
        try {
            Files.createDirectory(directory.resolve("permission-probe"));
        } catch (final IOException expected) {
            blocked = true;
        }
        Assumptions.assumeTrue(blocked, "cannot make a directory unwritable here; running as root?");
    }

    /** Give write permission back, so the temporary directory can be cleaned up. */
    private static void makeWritable(final Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            return;
        }
        Files.setPosixFilePermissions(directory, PosixFilePermissions.fromString("rwxr-xr-x"));
        final List<Path> children;
        try (Stream<Path> files = Files.list(directory)) {
            children = files.filter(Files::isDirectory).toList();
        }
        for (final Path child : children) {
            makeWritable(child);
        }
    }
}
