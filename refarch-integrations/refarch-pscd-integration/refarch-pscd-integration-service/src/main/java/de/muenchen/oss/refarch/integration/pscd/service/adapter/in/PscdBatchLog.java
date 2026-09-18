package de.muenchen.oss.refarch.integration.pscd.service.adapter.in;

import de.muenchen.oss.refarch.integration.pscd.domain.exception.PscdValidationException;
import de.muenchen.oss.refarch.integration.pscd.domain.model.PscdSatzarten;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart010;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import org.slf4j.Logger;

/**
 * Logging helpers shared by the inbound adapters (SOAP, REST, file).
 *
 * <p>
 * A PSCD batch is accepted fire-and-forget, so the log line written at the inbound edge is the only
 * record of what arrived. This class keeps that line uniform across the three channels.
 * </p>
 *
 * <p>
 * {@link #logAccepted} takes the caller's {@link Logger} on purpose: the log category stays the
 * adapter class, so {@code logging.level} remains controllable per channel, while the message
 * wording lives in one place.
 * </p>
 */
public final class PscdBatchLog {

    public static final String NO_FILENAME = "<none>";

    private static final int MAX_FILENAME_LENGTH = 255;

    private static final String TRUNCATION_MARKER = "...";

    /** Control characters, most notably CR and LF, which would forge additional log lines. */
    private static final Pattern CONTROL_CHARACTERS = Pattern.compile("\\p{Cntrl}");

    private PscdBatchLog() {
    }

    /**
     * Make a caller-supplied batch filename safe to log: the REST and SOAP payloads carry it verbatim,
     * so it is untrusted input. Control characters (CR/LF above all, which would otherwise forge
     * additional log lines) are replaced and the value is truncated.
     *
     * @param filename the raw filename (may be {@code null})
     * @return a single-line, length-bounded replacement, never {@code null}
     */
    public static String safeFilename(final String filename) {
        if (filename == null || filename.isBlank()) {
            return NO_FILENAME;
        }
        final String sanitized = CONTROL_CHARACTERS.matcher(filename).replaceAll("?");
        if (sanitized.length() <= MAX_FILENAME_LENGTH) {
            return sanitized;
        }
        return sanitized.substring(0, MAX_FILENAME_LENGTH) + TRUNCATION_MARKER;
    }

    /**
     * Describe the content of a batch in one compact fragment, e.g.
     * {@code 22 records (010=1, 100=3, 250=18), abstimmsumme='12345' vorzeichen='+'}. Only record types
     * that actually occur are listed. The control values are the ones the receiving side reconciles
     * against; no record content is included.
     *
     * @param batch the batch to describe (may be {@code null})
     * @return the description, never {@code null}
     */
    public static String describe(final PscdSatzarten batch) {
        if (batch == null) {
            return "no batch";
        }
        final StringJoiner breakdown = new StringJoiner(", ");
        int total = 0;
        total += count(breakdown, "010", batch.getSatzart010() == null ? 0 : 1);
        total += count(breakdown, "100", batch.getSatzart100());
        total += count(breakdown, "105", batch.getSatzart105());
        total += count(breakdown, "155", batch.getSatzart155());
        total += count(breakdown, "165", batch.getSatzart165());
        total += count(breakdown, "200", batch.getSatzart200());
        total += count(breakdown, "210", batch.getSatzart210());
        total += count(breakdown, "250", batch.getSatzart250());
        total += count(breakdown, "260", batch.getSatzart260());
        total += count(breakdown, "FEHLER", batch.getSatzartFehler());

        final StringBuilder description = new StringBuilder();
        description.append(total).append(total == 1 ? " record" : " records");
        if (total > 0) {
            description.append(" (").append(breakdown).append(')');
        }
        return description.append(", ").append(describeControlRecord(batch.getSatzart010())).toString();
    }

    /**
     * Log an accepted batch at the inbound edge: one INFO line naming the channel and the content, plus
     * a WARN for each condition that needs an operator's attention.
     *
     * @param log the calling adapter's logger, so the log category stays that adapter
     * @param channel the inbound channel the batch arrived on, e.g. {@code REST}
     * @param batch the decoded batch (may be {@code null})
     */
    public static void logAccepted(final Logger log, final String channel, final PscdSatzarten batch) {
        final String filename = safeFilename(batch == null ? null : batch.getFilename());
        log.info("Received PSCD batch '{}' via {}: {}", filename, channel, describe(batch));
        if (batch == null) {
            return;
        }
        final int errorRecords = size(batch.getSatzartFehler());
        if (errorRecords > 0) {
            log.warn("PSCD batch '{}' carries {} in-band error record(s)", filename, errorRecords);
        }
        if (batch.getSatzart010() == null) {
            // Only reachable via the file channel; the canonical mapper rejects such a batch outright.
            log.warn("PSCD batch '{}' has no Satzart010 control record", filename);
        }
    }

    /**
     * Log a batch the canonical mapper refused. Each inbound adapter reports this itself: the mapper is
     * shared by all of them and knows neither the channel nor whether anything upstream will log. The
     * REST edge turns the rejection into a {@code 400}, the SOAP edge into a fault, and neither logs on
     * its own.
     *
     * @param log the calling adapter's logger, so the log category stays that adapter
     * @param channel the inbound channel the batch arrived on, e.g. {@code REST}
     * @param filename the raw filename of the rejected batch (may be {@code null})
     * @param e the rejection, whose message states what was structurally wrong
     */
    public static void logRejected(final Logger log, final String channel, final String filename, final PscdValidationException e) {
        log.warn("Rejected invalid PSCD batch '{}' received via {}: {}", safeFilename(filename), channel, e.getMessage());
    }

    /** Add {@code code=size} to the breakdown when the record type occurs, and report its size. */
    private static int count(final StringJoiner breakdown, final String code, final List<?> records) {
        return count(breakdown, code, size(records));
    }

    private static int count(final StringJoiner breakdown, final String code, final int size) {
        if (size > 0) {
            breakdown.add(code + "=" + size);
        }
        return size;
    }

    private static String describeControlRecord(final Satzart010 controlRecord) {
        if (controlRecord == null) {
            return "no control record";
        }
        return "abstimmsumme='%s' vorzeichen='%s'".formatted(controlRecord.getAbstimmsumme(), controlRecord.getVorzeichen());
    }

    private static int size(final List<?> records) {
        return records == null ? 0 : records.size();
    }
}
