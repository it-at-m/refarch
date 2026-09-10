package de.muenchen.oss.refarch.integration.pscd.service.account;

import de.muenchen.oss.refarch.integration.pscd.domain.model.PscdSatzarten;
import de.muenchen.oss.refarch.integration.pscd.service.adapter.in.PscdBatchLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The two accounting trails, each written to a file of its own:
 * {@code logs/account-error.log} and {@code logs/completion.log}.
 *
 * <p>
 * They are separate from the service log on purpose. The service log is for whoever runs the
 * application; these two are the record of what happened to the batches: which records could not be
 * processed as they arrived, and which records were handed to PSCD. The predecessor application
 * kept the same two trails, and the reconciliation around them is built on their content.
 * </p>
 *
 * <p>
 * The logger names here are what {@code logback-spring.xml} binds the file appenders to, and both
 * are declared {@code additivity="false"} there, so nothing of this reaches the console. Written
 * through SLF4J like everything else, so the appenders decide where it lands, which is why the log
 * directory is configurable ({@code PSCD_LOG_DIR}) without a code change.
 * </p>
 */
@SuppressWarnings("PMD.MoreThanOneLogger")
public final class PscdAccountLog {

    /** Bound to {@code logs/account-error.log}: one line per record that arrived unusable. */
    private static final Logger ACCOUNT_ERROR = LoggerFactory.getLogger("pscd.account-error");

    /** Bound to {@code logs/completion.log}: one line per file handed to PSCD. */
    private static final Logger COMPLETION = LoggerFactory.getLogger("pscd.completion");

    private PscdAccountLog() {
    }

    /**
     * Report one problem found in a batch: a line whose SATZART is unknown, or a record that did not
     * arrive complete. One line per problem, so the file can be counted rather than parsed.
     *
     * @param filename the batch the problem was found in
     * @param problem what was wrong, already carrying the line number
     */
    public static void accountError(final String filename, final String problem) {
        ACCOUNT_ERROR.error("batch='{}' {}", PscdBatchLog.safeFilename(filename), problem);
    }

    /**
     * Report a file as delivered: one line, whatever the batch held, because that is the line the
     * reconciliation reads per file.
     *
     * <p>
     * Both numbers are on it: how many entries went to PSCD, and how many the file had. They are equal
     * for an ordinary batch, since every non-blank line becomes either a record or an error record, and
     * differ when a line replaced an earlier one: a second control record overwrites the first, so that
     * batch sends one entry fewer than the file held.
     * </p>
     *
     * @param batch the batch as it was handed to the outbound port
     * @param entriesInFile how many non-blank lines the file carried
     */
    public static void completed(final PscdSatzarten batch, final int entriesInFile) {
        COMPLETION.info("{} of {} entries from the file '{}' sent to PSCD",
                entriesSent(batch), entriesInFile, PscdBatchLog.safeFilename(batch.getFilename()));
    }

    /**
     * Everything the batch carries, error records included: they are sent too, and PSCD counts them.
     */
    private static int entriesSent(final PscdSatzarten batch) {
        return (batch.getSatzart010() == null ? 0 : 1)
                + batch.getSatzart100().size()
                + batch.getSatzart105().size()
                + batch.getSatzart155().size()
                + batch.getSatzart165().size()
                + batch.getSatzart200().size()
                + batch.getSatzart210().size()
                + batch.getSatzart250().size()
                + batch.getSatzart260().size()
                + batch.getSatzartFehler().size();
    }
}
