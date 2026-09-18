package de.muenchen.oss.refarch.integration.pscd.service.adapter.in.file;

import de.muenchen.oss.refarch.integration.pscd.application.port.in.SubmitPscdBatchInPort;
import de.muenchen.oss.refarch.integration.pscd.domain.model.PscdSatzarten;
import de.muenchen.oss.refarch.integration.pscd.service.account.PscdAccountLog;
import de.muenchen.oss.refarch.integration.pscd.service.adapter.in.PscdBatchLog;
import de.muenchen.oss.refarch.integration.pscd.service.adapter.in.PscdInboundChannel;
import de.muenchen.oss.refarch.integration.pscd.service.adapter.out.notification.PscdFailureNotifier;
import de.muenchen.oss.refarch.integration.pscd.service.configuration.PscdInboundProperties;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * File inbound adapter: polls a directory for flat-file PSCD batches, decodes them into the domain
 * aggregate, and hands them to the inbound port.
 *
 * <p>
 * The flat-file format is a file-channel concern, so the decoding ({@link PscdSatzartenParser})
 * lives here in the adapter, never in the core. Disabled by default; enabled by
 * {@code refarch.pscd.inbound.file.enabled}.
 * </p>
 *
 * <p>
 * <strong>A batch passes through three directories</strong>, all children of the polled one and
 * none
 * of them polled (the poll only picks up regular files directly in the polled directory):
 * {@code refarch.pscd.inbound.file.working-directory} ({@code .working}) while it is being
 * processed, then {@code .done-directory} ({@code .done}) or {@code .error-directory}
 * ({@code .error}) depending on how that went.
 * </p>
 *
 * <p>
 * <strong>Taken up before it is delivered.</strong> The move into the working directory happens
 * first, before the file is even read, and that ordering is what makes "delivered at most once"
 * hold: from that moment the poll cannot see the file again, so a later failure to file it as done
 * cannot bring it back for a second delivery. The cost is that a batch whose processing is
 * interrupted (a failed final move, a crash, a kill) stays in the working directory and is
 * <em>not</em> retried; {@link #reportHeldBatches()} reports those at startup for a human to
 * settle, because a batch left there may or may not already be at PSCD, and re-sending an
 * accounting batch on that guess is worse than having someone look.
 * </p>
 *
 * <p>
 * <strong>Only files that have stopped changing are taken up.</strong> A batch is picked up once
 * its size and modification time have held still for {@code refarch.pscd.inbound.file.stable-for}.
 * Otherwise a file still being written would be read half-finished and, because the parser repairs
 * a short record rather than rejecting it, delivered as a batch full of {@code REQUIRED}
 * placeholders instead of stopped. This is a heuristic; see that property for the sound alternative
 * and for how to switch the wait off.
 * </p>
 *
 * <p>
 * The file is stamped with the time it was taken up, before its extension ({@code batch.txt}
 * becomes
 * {@code batch_20260804_161500123.txt}), and keeps that name through the working directory into its
 * archive, so a filename the sending side reuses does not overwrite the copy of an earlier run.
 * </p>
 */
@Configuration
@ConditionalOnProperty(name = "refarch.pscd.inbound.file.enabled", havingValue = "true")
@EnableScheduling
@Slf4j
public class PscdFilePoller {

    private static final PscdInboundChannel CHANNEL = PscdInboundChannel.FILE;

    /**
     * Stamp given to a batch as it is taken up, in the local time zone of the polling process.
     * Millisecond resolution, because two batches of the same name taken up within one second would
     * otherwise be filed under the same name and the second would replace the first, losing the
     * archived copy of a batch that has already been delivered.
     */
    private static final DateTimeFormatter TAKEN_UP_AT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");

    /**
     * How long a file may keep changing before its transfer is called stalled and said so once, out
     * loud. The wait itself is only reported at {@code DEBUG}, which nobody is reading when a batch
     * has silently failed to arrive.
     */
    private static final Duration MIN_STALL_REPORT_DELAY = Duration.ofMinutes(1);

    /**
     * …but never sooner than this many polls, so a deployment polling on a long interval does not call
     * a file stalled before it has had a couple of chances to settle.
     */
    private static final int MIN_STALL_REPORT_POLLS = 10;

    /**
     * The settle time at which the check is switched off and a file is taken up the moment it is seen.
     */
    private static final long SETTLE_CHECK_OFF = 0L;

    private final SubmitPscdBatchInPort submitPscdBatchInPort;
    private final PscdFailureNotifier failureNotifier;
    private final Path directory;
    private final String workingDirectory;
    private final String doneDirectory;
    private final String errorDirectory;
    private final Charset charset;

    /** {@code refarch.pscd.inbound.file.stable-for} in nanos; {@code 0} takes files up on sight. */
    private final long stableForNanos;

    /**
     * How long a file may go on changing before {@link #reportNotYetStable} says so at {@code WARN}.
     */
    private final long stallReportAfterNanos;

    /**
     * Whether the polled directory was absent on the previous cycle, so that its absence and its
     * reappearance are each logged once instead of on every cycle. Only ever touched by the scheduler.
     */
    private boolean inboxMissing;

    /**
     * What each file in the inbox looked like when it was last examined, which is how a file that is
     * still being written is told from one that has settled. Only ever touched by the scheduler, and
     * pruned every cycle to the files actually present, so it cannot outgrow the inbox.
     */
    private final Map<Path, Observation> observations = new HashMap<>();

    /**
     * Files whose take-up failed and which have already been reported, so a broken inbox produces one
     * {@code ERROR} per file rather than one per poll. Pruned alongside {@link #observations}.
     */
    private final Set<Path> reportedTakeUpFailures = new HashSet<>();

    public PscdFilePoller(final SubmitPscdBatchInPort submitPscdBatchInPort, final PscdFailureNotifier failureNotifier,
            final PscdInboundProperties properties) {
        final PscdInboundProperties.FileProperties file = properties.getFile();
        this.submitPscdBatchInPort = submitPscdBatchInPort;
        this.failureNotifier = failureNotifier;
        this.directory = Path.of(file.getDirectory());
        this.workingDirectory = file.getWorkingDirectory();
        this.doneDirectory = file.getDoneDirectory();
        this.errorDirectory = file.getErrorDirectory();
        this.charset = file.getCharset();
        this.stableForNanos = file.getStableFor() == null ? 0L : Math.max(0L, file.getStableFor().toNanos());
        this.stallReportAfterNanos = Math.max(MIN_STALL_REPORT_DELAY.toNanos(),
                TimeUnit.MILLISECONDS.toNanos(MIN_STALL_REPORT_POLLS * file.getPollInterval()));
        log.info("PSCD file inbound enabled: polling '{}' every {} ms, decoding as {}, taking up files that have not "
                + "changed for {} ms", this.directory.toAbsolutePath(), file.getPollInterval(), this.charset.name(),
                TimeUnit.NANOSECONDS.toMillis(this.stableForNanos));
    }

    /**
     * Report anything left in the working directory by an earlier run. Deliberately a report and
     * nothing more: a batch is taken up before it is delivered, so one found here is one whose
     * delivery status is unknown, and picking it up again would risk posting it to PSCD twice.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void reportHeldBatches() {
        final Path working = this.directory.resolve(this.workingDirectory);
        if (!Files.isDirectory(working)) {
            return;
        }
        final List<String> held = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(working, Files::isRegularFile)) {
            stream.forEach(file -> held.add(PscdBatchLog.safeFilename(file.getFileName().toString())));
        } catch (final IOException e) {
            log.warn("Could not list the PSCD working directory '{}'", working.toAbsolutePath(), e);
            return;
        }
        if (held.isEmpty()) {
            return;
        }
        log.warn("PSCD working directory '{}' holds {} batch(es) from an earlier run whose delivery status is unknown; "
                + "they are not picked up again and need settling by hand: {}",
                working.toAbsolutePath(), held.size(), String.join(", ", held));
    }

    // The default below must stay in sync with PscdInboundProperties.FileProperties#pollInterval, which
    // is the value reported at startup.
    @Scheduled(fixedDelayString = "${refarch.pscd.inbound.file.poll-interval:1000}")
    public void poll() {
        if (!Files.isDirectory(this.directory)) {
            if (!this.inboxMissing) {
                this.inboxMissing = true;
                log.warn("PSCD inbox '{}' does not exist; no batches are being polled", this.directory.toAbsolutePath());
            }
            return;
        }
        if (this.inboxMissing) {
            this.inboxMissing = false;
            log.info("PSCD inbox '{}' exists again; resuming polling", this.directory.toAbsolutePath());
        }
        final List<Path> candidates = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(this.directory, Files::isRegularFile)) {
            stream.forEach(candidates::add);
        } catch (final IOException e) {
            log.warn("Could not list PSCD inbox '{}'", this.directory.toAbsolutePath(), e);
            return;
        }
        forgetVanished(candidates);
        if (!candidates.isEmpty()) {
            log.debug("Polled PSCD inbox '{}': {} file(s) to consider", this.directory.toAbsolutePath(), candidates.size());
        }
        for (final Path file : candidates) {
            if (!hasStoppedChanging(file)) {
                continue;
            }
            // Out of the poll's sight before anything is read or delivered: see the class Javadoc.
            final Path takenUp = takeUp(file);
            if (takenUp != null) {
                process(takenUp, file.getFileName().toString());
            }
        }
    }

    /**
     * Drop what is remembered about files that are no longer in the inbox, so neither the settle
     * bookkeeping nor the reported-failure set can grow without bound over a long-running service.
     */
    private void forgetVanished(final List<Path> present) {
        if (this.observations.isEmpty() && this.reportedTakeUpFailures.isEmpty()) {
            return;
        }
        final Set<Path> stillThere = Set.copyOf(present);
        this.observations.keySet().retainAll(stillThere);
        this.reportedTakeUpFailures.retainAll(stillThere);
    }

    /**
     * Whether the file's size and modification time have held still long enough for it to be read.
     *
     * <p>
     * A first sighting never qualifies: one look says nothing about whether the write has finished, so
     * a file needs at least two polls whatever {@code stable-for} is, except at {@code 0}, which
     * switches the check off and takes files up on sight (the right setting when the sending side
     * renames a finished file into place, see the property's own documentation).
     * </p>
     */
    private boolean hasStoppedChanging(final Path file) {
        if (this.stableForNanos == SETTLE_CHECK_OFF) {
            return true;
        }
        final BasicFileAttributes attributes;
        try {
            attributes = Files.readAttributes(file, BasicFileAttributes.class);
        } catch (final IOException e) {
            // Vanished or unreadable between the listing and now; nothing has been touched, so the next
            // cycle can simply look again.
            log.debug("Could not read the attributes of PSCD batch '{}'; leaving it for the next poll",
                    PscdBatchLog.safeFilename(file.getFileName().toString()), e);
            return false;
        }
        final long now = System.nanoTime();
        final Observation observation = this.observations.get(file);
        if (observation == null) {
            this.observations.put(file, new Observation(attributes.size(), attributes.lastModifiedTime().toMillis(), now));
            log.debug("PSCD batch '{}' seen for the first time; waiting for it to stop changing",
                    PscdBatchLog.safeFilename(file.getFileName().toString()));
            return false;
        }
        if (observation.changedTo(attributes.size(), attributes.lastModifiedTime().toMillis(), now)
                || now - observation.unchangedSinceNanos < this.stableForNanos) {
            reportNotYetStable(file, observation, now);
            return false;
        }
        return true;
    }

    /**
     * Say that a file is not ready yet: at {@code DEBUG} while that is ordinary, and once at
     * {@code WARN} when it has gone on long enough to be a stalled transfer rather than a busy one.
     */
    private void reportNotYetStable(final Path file, final Observation observation, final long now) {
        final String filename = PscdBatchLog.safeFilename(file.getFileName().toString());
        if (observation.stallReported || now - observation.firstSeenNanos < this.stallReportAfterNanos) {
            log.debug("PSCD batch '{}' is still changing; not taking it up yet", filename);
            return;
        }
        observation.reportStalled();
        log.warn("PSCD batch '{}' has been in the inbox for over {} s without settling; the transfer looks stalled, "
                + "and nothing is delivered until it stops changing",
                filename, TimeUnit.NANOSECONDS.toSeconds(this.stallReportAfterNanos));
    }

    /**
     * Move the file into the working directory under its stamped name and return where it landed, or
     * {@code null} if it could not be taken up, in which case nothing has been read or delivered and
     * the file is untouched, so the next cycle simply tries again.
     */
    private Path takeUp(final Path file) {
        final String filename = file.getFileName().toString();
        try {
            final Path working = this.directory.resolve(this.workingDirectory);
            Files.createDirectories(working);
            final Path takenUp = working.resolve(stamped(filename));
            Files.move(file, takenUp, StandardCopyOption.REPLACE_EXISTING);
            this.observations.remove(file);
            this.reportedTakeUpFailures.remove(file);
            log.debug("Took up PSCD batch '{}' as '{}'", PscdBatchLog.safeFilename(filename),
                    PscdBatchLog.safeFilename(this.workingDirectory + '/' + takenUp.getFileName()));
            return takenUp;
        } catch (final IOException e) {
            if (this.reportedTakeUpFailures.add(file)) {
                // Once per file rather than once per poll: at a one-second interval a broken inbox would
                // otherwise bury every other line in the log.
                log.error("Could not take up PSCD batch '{}' into {}; nothing has been delivered and the file is "
                        + "untouched. Each poll will try again.", PscdBatchLog.safeFilename(filename), this.workingDirectory, e);
            }
            return null;
        }
    }

    /**
     * Read, deliver and file one batch that has already been taken up.
     *
     * @param takenUp where the batch now lives, under its stamped name
     * @param filename the name it arrived under, which is the batch's identity end to end; the stamp
     *            belongs to the archive, not to the batch
     */
    private void process(final Path takenUp, final String filename) {
        try {
            final List<String> lines = splitLines(Files.readString(takenUp, this.charset));
            final PscdSatzartenParser.Result parsed = PscdSatzartenParser.parseBatch(lines, filename);
            final PscdSatzarten batch = parsed.batch();
            PscdBatchLog.logAccepted(log, CHANNEL.label(), batch);
            this.submitPscdBatchInPort.submit(batch);
            // Only once delivery returned: the completion trail is the record of what PSCD received,
            // so a batch that failed on the way out must not appear in it. The file's own entry count
            // comes from here rather than from the parser, because it is a property of the file.
            PscdAccountLog.completed(batch, entriesIn(lines));
            final String archivedAs = archive(takenUp, this.doneDirectory, filename);
            if (!parsed.problems().isEmpty()) {
                // A delivered batch can still carry records PSCD could not be given as they arrived.
                // Notified after the move, so the mail can name the archive the file went to, and only
                // when there is something to report, so a clean batch stays silent.
                this.failureNotifier.notifyRecordErrors(CHANNEL, filename, archivedAs, parsed.problems());
            }
        } catch (final Exception e) {
            log.error("Failed to process PSCD batch file '{}'; filing it under {}",
                    PscdBatchLog.safeFilename(filename), this.errorDirectory, e);
            final String archivedAs = archive(takenUp, this.errorDirectory, filename);
            // After the move, so the mail can name where the batch actually ended up.
            this.failureNotifier.notifyFailure(CHANNEL, filename, archivedAs, e);
        }
    }

    /**
     * File a processed batch under the given subdirectory, keeping the name it was stamped with when it
     * was taken up, and return where it can be found now.
     *
     * <p>
     * A failure is logged rather than thrown, so one unfilable batch cannot abort the rest of the poll
     * cycle, and the returned location is then the working directory, where the file genuinely still
     * is, which is what a failure mail should say. Either way the batch is out of the polled directory,
     * so it is not delivered a second time; the target subdirectory only decides which consequence to
     * report, because the done directory means the batch already reached PSCD.
     * </p>
     */
    private String archive(final Path takenUp, final String subdir, final String filename) {
        final String heldAs = this.workingDirectory + '/' + takenUp.getFileName();
        try {
            final Path target = this.directory.resolve(subdir);
            Files.createDirectories(target);
            final Path destination = target.resolve(takenUp.getFileName());
            // REPLACE_EXISTING still matters: the stamp is only as fine as a millisecond, so two
            // batches of the same name taken up within one would otherwise fail here rather than move.
            Files.move(takenUp, destination, StandardCopyOption.REPLACE_EXISTING);
            final String archived = subdir + '/' + destination.getFileName();
            log.debug("Filed PSCD batch '{}' under '{}'", PscdBatchLog.safeFilename(filename), PscdBatchLog.safeFilename(archived));
            return archived;
        } catch (final IOException e) {
            final String safeFilename = PscdBatchLog.safeFilename(filename);
            if (subdir.equals(this.doneDirectory)) {
                log.error("Delivered PSCD batch '{}' but could not file it under {}; it is held in '{}' and will NOT be "
                        + "delivered again; file it by hand once you have checked it reached PSCD.",
                        safeFilename, subdir, PscdBatchLog.safeFilename(heldAs), e);
            } else {
                log.error("Could not file the failed PSCD batch '{}' under {}; it is held in '{}' and is not retried.",
                        safeFilename, subdir, PscdBatchLog.safeFilename(heldAs), e);
            }
            return heldAs;
        }
    }

    /**
     * The name a batch is filed under: the time it was taken up, inserted before the extension, so
     * {@code batch.txt} becomes {@code batch_20260804_161500123.txt} and the sending side reusing a
     * filename cannot overwrite an earlier run's copy.
     *
     * <p>
     * A name with nothing to insert before, meaning no extension, a leading dot and no other
     * ({@code .keep}), or a trailing dot, simply gets the stamp appended.
     * </p>
     */
    private static String stamped(final String filename) {
        final String stamp = '_' + LocalDateTime.now().format(TAKEN_UP_AT);
        final int extension = filename.lastIndexOf('.');
        if (extension <= 0 || extension == filename.length() - 1) {
            return filename + stamp;
        }
        return filename.substring(0, extension) + stamp + filename.substring(extension);
    }

    /**
     * Split the file content into record lines. Blank lines are kept so that the parser can report
     * physical line numbers; it skips them itself.
     */
    private static List<String> splitLines(final String raw) {
        return raw == null ? List.of() : List.of(raw.split("\\R"));
    }

    /**
     * How many entries the file carried: the lines the parser will look at, so blanks do not count.
     */
    private static int entriesIn(final List<String> lines) {
        return (int) lines.stream().filter(line -> !line.isBlank()).count();
    }

    /** How many inbox files the settle check is currently remembering. Package-private for its test. */
    /* default */ int trackedFiles() {
        return this.observations.size();
    }

    /**
     * What one inbox file looked like when it was last examined, and since when it has looked that way.
     */
    private static final class Observation {

        private long size;
        private long modifiedMillis;

        private long unchangedSinceNanos;

        private final long firstSeenNanos;

        private boolean stallReported;

        private Observation(final long size, final long modifiedMillis, final long nowNanos) {
            this.size = size;
            this.modifiedMillis = modifiedMillis;
            this.unchangedSinceNanos = nowNanos;
            this.firstSeenNanos = nowNanos;
        }

        /**
         * Take a fresh look at the file and say whether it differs from the last one, restarting the
         * settle clock when it does.
         */
        private boolean changedTo(final long size, final long modifiedMillis, final long nowNanos) {
            if (this.size == size && this.modifiedMillis == modifiedMillis) {
                return false;
            }
            this.size = size;
            this.modifiedMillis = modifiedMillis;
            this.unchangedSinceNanos = nowNanos;
            return true;
        }

        private void reportStalled() {
            this.stallReported = true;
        }
    }
}
