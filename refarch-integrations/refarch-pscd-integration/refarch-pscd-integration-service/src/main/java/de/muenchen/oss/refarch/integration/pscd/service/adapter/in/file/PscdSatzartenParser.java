package de.muenchen.oss.refarch.integration.pscd.service.adapter.in.file;

import de.muenchen.oss.refarch.integration.pscd.domain.exception.PscdValidationException;
import de.muenchen.oss.refarch.integration.pscd.domain.model.PscdSatzarten;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart010;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart100;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart105;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart155;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart165;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart200;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart210;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart250;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart260;
import de.muenchen.oss.refarch.integration.pscd.domain.model.SatzartFehler;
import de.muenchen.oss.refarch.integration.pscd.domain.validation.PscdSatzartenValidator;
import de.muenchen.oss.refarch.integration.pscd.service.account.PscdAccountLog;
import de.muenchen.oss.refarch.integration.pscd.service.adapter.in.PscdBatchLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

/**
 * Parses the raw record lines of a PSCD batch into the {@link PscdSatzarten} domain aggregate.
 *
 * <p>
 * <strong>Interchange format</strong>: one fixed-length record per line, with no field separator.
 * The leading SATZART column carries the record code ({@code 010}, {@code 100}, {@code 105},
 * {@code 155}, {@code 165}, {@code 200}, {@code 210}, {@code 250}, {@code 260}) and selects the
 * column layout for the rest of the line. Every further field is addressed by the 1-based columns
 * it spans, start and end inclusive, and is space-padded within them.
 * </p>
 *
 * <p>
 * A record also carries fields this integration does not need. Those are simply not declared, so a
 * layout's columns need be neither contiguous nor in ascending order, and two of them may cover the
 * same characters where the spec feeds two domain fields from one region.
 * </p>
 *
 * <p>
 * Anything beyond the last column a record maps is ignored. Some columns are declared
 * {@code trailing}, meaning the record legitimately stops before them. SATZART 210 and 260 arrive
 * in two lengths, 419 and 445 characters, differing only in whether the optional KOSTL / AUFNR /
 * MWSKZ block is present. The batch {@code Filename} is the polled file's name, passed by
 * {@code PscdFilePoller}.
 * </p>
 *
 * <p>
 * FEHLER is not a column. It is the field the receiving side fills, so no layout declares it. This
 * parser writes it, and {@link PscdSatzarten#getSatzartFehler()}, only to report what was wrong
 * with a line; the next two paragraphs are the whole of it.
 * </p>
 *
 * <p>
 * <strong>Missing mandatory fields are repaired, not rejected, by design, carried over from the
 * legacy application.</strong> Where a record leaves a mandatory column empty, the field is filled
 * with {@value #REQUIRED_PLACEHOLDER} and the record's FEHLER names the columns it happened to. The
 * predecessor behaved this way and the receiving side is built around it: the batch is delivered
 * and PSCD decides what to do with a record marked so. Rejecting the file instead, which is what
 * the SOAP and REST channels do with the same defect, would stop batches the old system accepted.
 * </p>
 *
 * <p>
 * <strong>A damaged line does not fail the batch either</strong>, for the same reason. A line too
 * short for its record is sliced as far as it reaches, the columns beyond it are treated as empty
 * (mandatory ones becoming {@value #REQUIRED_PLACEHOLDER}), and its FEHLER opens with
 * {@code TRUNCATED}. A line whose SATZART cannot be read, or names no known record type, has no
 * layout to be sliced with at all: it becomes a {@link SatzartFehler} naming the line number and
 * the problem, which is the vendor contract's own way of reporting a record that could not be
 * processed. Either way the batch goes to PSCD.
 * </p>
 *
 * <p>
 * What is <em>counted</em> as a problem, summarised in one {@code WARN} line and written to the
 * accounting trail, is narrower than what is noted in a record: an unreadable or unknown
 * SATZART,
 * and a mandatory field that had to be filled in. A line that stops short of columns its record
 * type does not require loses nothing, so it is not an error, however short it is. This
 * is the predecessor's definition, and the reconciliation counts depend on it.
 * </p>
 *
 * <p>
 * The trade-off is deliberate and worth knowing: nothing here holds a batch back. A file decoded
 * with the wrong charset, or written against a different layout, will be delivered as records full
 * of shifted values rather than stopped for a human to look at. Its FEHLER fields and the
 * {@code WARN} are what make that visible.
 * </p>
 */
// Coupling and size are deliberate; naming each one is what makes the table checkable against the spec.
@SuppressWarnings({ "PMD.CouplingBetweenObjects", "PMD.AvoidDuplicateLiterals", "PMD.GodClass" })
@Slf4j
public final class PscdSatzartenParser {

    /**
     * Last column of the leading SATZART field, which always starts at column 1. Named separately
     * because the code has to be read before a layout can be chosen; every layout reuses it, so it is
     * entered once.
     */
    private static final int SATZART_END = 3;

    /**
     * Currency stamped on every {@link Satzart210}. The 210 record does not carry BLWAE, so the value
     * is fixed here instead of being read from a column, the one field on this channel that does not
     * come from the line.
     */
    private static final String SATZART210_BLWAE = "EUR";

    /** How many problems the summary log line spells out before counting the remainder. */
    private static final int MAX_REPORTED_PROBLEMS = 500;

    /**
     * SATZART of the error record standing in for a line whose own code could not be read. Three
     * characters, the width the vendor contract allows.
     */
    private static final String UNREADABLE_SATZART = "???";

    /**
     * What a mandatory column the file left empty is filled with, and the prefix of the FEHLER that
     * reports it. Legacy behaviour: see the class Javadoc for why the record is repaired rather than
     * the batch rejected.
     */
    /* default */ static final String REQUIRED_PLACEHOLDER = "REQUIRED";

    /** Length the vendor contract allows FEHLER, so a record with many gaps cannot overrun it. */
    private static final int MAX_FEHLER_LENGTH = 256;

    /**
     * The fixed-width column layout per SATZART code.
     *
     * <p>
     * This table is the single place the PSCD record layout spec lives; each entry is a straight copy
     * of the spec's start and end column, both inclusive. Only the fields this integration maps are
     * declared; everything else in the record is deliberately absent, which is why the entries need
     * not be contiguous.
     * </p>
     *
     * <p>
     * The <em>declaration order</em>, however, is load-bearing: it must match the order in which the
     * matching {@code toSatzartNNN} builder below reads its fields, because that builder takes them
     * from a sequential cursor. The order here follows the domain model (the vendor WSDL sequence
     * minus FEHLER); reorder one and the other must move with it.
     * </p>
     */
    private static final Map<String, Layout> LAYOUTS = buildLayouts();

    /**
     * Columns whose transcribed positions are impossible: starting before column 1, or ending before
     * they start.
     */
    private static final List<String> INCONSISTENT_COLUMNS = findInconsistentColumns();

    private PscdSatzartenParser() {
    }

    /**
     * Parse the given record lines into a {@link PscdSatzarten} aggregate.
     *
     * <p>
     * Blank lines are skipped; every other line must be a well-formed fixed-length record. Line
     * numbers reported in failures are those of the lines as handed in, so the caller must not
     * pre-filter them ({@code PscdFilePoller} does not).
     * </p>
     *
     * @param lines the raw record lines (may be {@code null})
     * @param filename the batch file name to stamp on the aggregate; mandatory, since it identifies
     *            the batch end to end
     * @return the parsed aggregate (never {@code null})
     * @throws PscdValidationException if the filename is blank, the only content of a poll this
     *             method refuses, since a batch nobody can name cannot be traced afterwards. No
     *             defect in the <em>lines</em> throws: see the class Javadoc
     * @throws IllegalStateException if the column layout is transcribed wrongly
     */
    public static PscdSatzarten parse(final List<String> lines, final String filename) {
        return parseBatch(lines, filename).batch();
    }

    /**
     * What one parse produced: the batch, and the accounting problems found while reading it.
     *
     * @param batch the aggregate to deliver
     * @param problems one entry per record that could not be processed as it arrived, the same entries
     *            that went to {@code logs/account-error.log}, for a caller that also has to
     *            report them
     */
    public record Result(PscdSatzarten batch, List<String> problems) {
    }

    /**
     * Parse as {@link #parse} does, and hand back what was wrong with the batch as well, which the file
     * poller needs, because it notifies about record errors after the batch has been delivered.
     *
     * @param lines the raw record lines (may be {@code null})
     * @param filename the batch file name to stamp on the aggregate; mandatory
     * @return the batch and its problems, never {@code null}
     */
    public static Result parseBatch(final List<String> lines, final String filename) {
        requireConsistentLayout();
        if (filename == null || filename.isBlank()) {
            // The poller always passes the polled file's name; this turns that convention into an
            // invariant, so every caller of the port gets a batch that is identifiable end to end.
            throw new PscdValidationException("PSCD batch is missing the mandatory filename");
        }
        final RecordCollector collector = new RecordCollector();
        final List<String> problems = new ArrayList<>();
        final String logName = PscdBatchLog.safeFilename(filename);
        if (lines != null) {
            int lineNumber = 0;
            for (final String line : lines) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                parseLine(line, new LineContext(lineNumber, problems), collector);
            }
        }
        if (!problems.isEmpty()) {
            // Delivered regardless: the record carries the problem in its FEHLER. One summary line for
            // whoever runs the service, and one line per problem in the accounting trail the finance
            // department reconciles against.
            log.warn("PSCD batch '{}' {}", logName, summarise(problems));
            problems.forEach(problem -> PscdAccountLog.accountError(logName, problem));
        }
        return new Result(collector.toAggregate(filename), List.copyOf(problems));
    }

    /**
     * Sum the problems up for one log line.
     *
     * <p>
     * Bounded at {@value #MAX_REPORTED_PROBLEMS} entries: a thoroughly broken export would otherwise
     * put one entry there per record. The count is always exact even when the list is cut.
     * </p>
     */
    private static String summarise(final List<String> problems) {
        final List<String> reported = problems.size() > MAX_REPORTED_PROBLEMS
                ? problems.subList(0, MAX_REPORTED_PROBLEMS)
                : problems;
        final String omitted = problems.size() > MAX_REPORTED_PROBLEMS
                ? " … and %d more".formatted(problems.size() - MAX_REPORTED_PROBLEMS)
                : "";
        return "has %d content problem(s), delivered with FEHLER set: %s%s"
                .formatted(problems.size(), String.join("; ", reported), omitted);
    }

    /**
     * Decode one non-blank line and hand the record to the collector.
     *
     * <p>
     * Nothing here fails the batch. A line whose SATZART cannot be read or does not name a known
     * record type has no layout to be sliced with, so it cannot become a typed record. It becomes a
     * standalone error record instead ({@link SatzartFehler}, the vendor contract's own vehicle for
     * this), and travels to PSCD saying what was wrong with it. A line that <em>is</em> mappable but
     * too short is sliced as far as it goes; see {@link #truncation}.
     * </p>
     */
    private static void parseLine(final String line, final LineContext context, final RecordCollector collector) {
        // SATZART starts at column 1, so its end column and its length are the same number.
        if (line.length() < SATZART_END) {
            collector.addUnmappable(UNREADABLE_SATZART, "line is %d character(s) long, too short to hold the %d-character SATZART column"
                    .formatted(line.length(), SATZART_END), context);
            return;
        }
        final String code = line.substring(0, SATZART_END).strip();
        if (code.isEmpty()) {
            collector.addUnmappable(UNREADABLE_SATZART, "SATZART column is blank", context);
            return;
        }
        final Layout layout = LAYOUTS.get(code);
        if (layout == null) {
            // In a fixed-width file an unknown code usually means the columns are misaligned rather
            // than that one record type is unsupported, which the error record says, so the receiving
            // side can tell this apart from a record it simply does not know.
            collector.addUnmappable(code, "unknown SATZART code '%s'".formatted(safe(code)), context);
            return;
        }
        final Fields fields = slice(line, layout, truncation(line, layout));
        // What counts as an accounting error is a record that could not be processed as it arrived:
        // here, a mandatory field the file left empty. A line that merely stops short of columns this
        // record does not have to carry costs nothing, so it is only noted in the record's FEHLER.
        for (final String missing : fields.filledIn) {
            context.problem("SATZART %s is missing %s, filled with %s"
                    .formatted(safe(code), missing, REQUIRED_PLACEHOLDER));
        }
        collector.add(code, fields, context);
    }

    /**
     * What is missing from the end of a line, as notes for the record's FEHLER. Empty when the line
     * reaches everything its record must carry.
     *
     * <p>
     * These are notes only, deliberately: a short line is not by itself an accounting error. Whether
     * anything was actually lost shows in what {@link #slice} had to fill in. A line that stops before
     * columns this record type does not require costs nothing, while one that stops before a mandatory
     * column is reported as that field being missing. Both cases still say {@code TRUNCATED} in the
     * record, because the receiving side can then see the line arrived incomplete.
     * </p>
     *
     * <p>
     * Nothing is asserted about what follows the last mapped column: the record may carry fields this
     * integration does not map, so content beyond is legitimate and cannot be required to be blank.
     * The cost is that a line truncated after the last mapped field looks complete; with gaps and
     * shared columns both legitimate, what actually pins the table to the spec is the complete sample
     * batch reconciling against its own checksum ({@code PscdSampleBatchChecksumTest}).
     * </p>
     */
    private static List<String> truncation(final String line, final Layout layout) {
        final List<String> notes = new ArrayList<>();
        if (line.length() < layout.requiredLength()) {
            notes.add("TRUNCATED: line is %d of %d characters".formatted(line.length(), layout.requiredLength()));
        }
        for (final Column column : layout.columns()) {
            // A trailing column is either wholly there or wholly absent. Ending inside one means the
            // record was cut mid-field, which no variant of the format produces, so the field is left
            // unset and the record says so.
            if (column.trailing() && line.length() > column.fromIndex() && line.length() < column.toIndex()) {
                notes.add("TRUNCATED: line ends inside %s".formatted(column.name()));
            }
        }
        return notes;
    }

    /**
     * Cut a line into its columns, each stripped of its padding, blank becoming {@code null}. Columns
     * are read at their own position, so regions between them are simply never touched, and a
     * trailing column the line stops short of yields {@code null}.
     *
     * <p>
     * A <em>mandatory</em> column the line left empty is the exception: it takes
     * {@value #REQUIRED_PLACEHOLDER} instead of {@code null}, and its name is remembered for the
     * record's FEHLER. Legacy behaviour, kept on purpose; see the class Javadoc.
     * </p>
     */
    private static Fields slice(final String line, final Layout layout, final List<String> notes) {
        final List<String> values = new ArrayList<>(layout.columns().size());
        final List<String> filledIn = new ArrayList<>();
        for (final Column column : layout.columns()) {
            final String value = column.toIndex() > line.length()
                    ? ""
                    : line.substring(column.fromIndex(), column.toIndex()).strip();
            if (value.isEmpty() && column.mandatory()) {
                values.add(REQUIRED_PLACEHOLDER);
                filledIn.add(column.name());
            } else if (value.isEmpty()) {
                // Blank stays unset rather than being emitted as an empty element downstream.
                values.add(null);
            } else {
                values.add(value);
            }
        }
        return new Fields(values, filledIn, notes);
    }

    /**
     * One line's position and the batch's violation list, so a check can record a problem and let the
     * walk carry on instead of aborting at the first bad line.
     */
    private record LineContext(int lineNumber, List<String> problems) {

        private void problem(final String detail) {
            this.problems.add("line %d: %s".formatted(this.lineNumber, detail));
        }
    }

    /** Keep a FEHLER or FEHLERTEXT within the length the vendor contract allows. */
    private static String capped(final String text) {
        return text.length() <= MAX_FEHLER_LENGTH ? text : text.substring(0, MAX_FEHLER_LENGTH);
    }

    /**
     * Make file-derived text safe to put in a message that ends up in a log line. Reuses the inbound
     * adapters' sanitizer: the concern is the same one, untrusted content forging log lines.
     */
    private static String safe(final String value) {
        return PscdBatchLog.safeFilename(value);
    }

    /**
     * Refuse to parse anything while a column sits at a position that cannot exist, because slicing
     * lines at wrong offsets would corrupt a batch silently, which is the failure this parser was
     * rewritten to remove.
     */
    private static void requireConsistentLayout() {
        if (!INCONSISTENT_COLUMNS.isEmpty()) {
            throw new IllegalStateException(
                    "The PSCD fixed-width column layout was transcribed wrongly; no batch can be parsed until it is "
                            + "corrected against the record layout spec. Problems: "
                            + String.join(", ", INCONSISTENT_COLUMNS));
        }
    }

    /** Package-private so the layout can be asserted against the spec and drive test fixtures. */
    /* default */ static Map<String, Layout> layoutsByCode() {
        return LAYOUTS;
    }

    private static Map<String, Layout> buildLayouts() {
        final Map<String, Layout> layouts = new LinkedHashMap<>();
        // CPD-OFF - the per-record column tables are intentionally repetitive
        layouts.put("010", Layout.ofColumns(
                mandatory("SATZART", 1, SATZART_END),
                mandatory("ABSTIMMSUMME", 24, 39),
                mandatory("VORZEICHEN", 40, 40)));
        layouts.put("100", Layout.ofColumns(
                mandatory("SATZART", 1, SATZART_END),
                mandatory("PSOBKEY", 4, 15),
                mandatory("PARTNER", 18, 27),
                column("ADDRNUM", 275, 284),
                column("PSOBTXTB1", 396, 449),
                column("PSOBTXTB2", 450, 503),
                column("PSOBTXTB3", 504, 557),
                column("PSOBTXTB4", 558, 611),
                column("PSOBTXTB5", 612, 665),
                column("PSOBTXTB6", 666, 719),
                column("ZWEITSCHULDNER", 721, 721),
                column("EIGENTUEMERWECHSEL", 722, 722),
                column("BETRIEBSENDE", 726, 733),
                column("KD_KENN", 754, 780),
                column("FACHDST_SB", 782, 811),
                column("FACHDST_TELNR", 812, 821)));
        layouts.put("105", Layout.ofColumns(
                mandatory("SATZART", 1, SATZART_END),
                mandatory("PSOBKEY", 4, 15),
                mandatory("CORR_PARTNER", 18, 27),
                mandatory("CORR_ROLE", 275, 276)));
        layouts.put("155", Layout.ofColumns(
                mandatory("SATZART", 1, SATZART_END),
                mandatory("PSOBKEY", 4, 15),
                column("DELETE_FLAG", 16, 16),
                column("CORR_ROLE", 264, 265)));
        layouts.put("165", Layout.ofColumns(
                mandatory("SATZART", 1, SATZART_END),
                mandatory("PSOBKEY", 4, 15),
                column("PSOBTXTB1", 28, 81),
                column("PSOBTXTB2", 82, 135),
                column("PSOBTXTB3", 136, 189),
                column("PSOBTXTB4", 190, 243),
                column("PSOBTXTB5", 244, 297),
                column("PSOBTXTB6", 298, 351),
                column("ZWEITSCHULDNER", 353, 353),
                column("EIGENTUEMERWECHSEL", 354, 354),
                column("BETRIEBSENDE", 358, 365),
                column("KD_KENN", 386, 412),
                column("FACHDST_SB", 414, 443),
                column("FACHDST_TELNR", 444, 453)));
        layouts.put("200", Layout.ofColumns(
                mandatory("SATZART", 1, SATZART_END),
                mandatory("PSOBKEY", 4, 15),
                mandatory("EINNAHMEART", 16, 20),
                mandatory("BETRW", 21, 31),
                mandatory("FAEDN", 32, 39),
                mandatory("BLDAT", 40, 47),
                column("PERSL", 48, 51),
                column("OPTXT", 64, 113),
                column("SGTXT", 64, 113),
                column("BLWAE", 398, 399),
                mandatory("XBLNR", 400, 411),
                mandatory("FV_BELNR", 412, 423),
                column("KOSTL", 426, 435),
                column("MWSKZ", 448, 449),
                column("AUFNR", 436, 447)));
        layouts.put("210", Layout.ofColumns(
                mandatory("SATZART", 1, SATZART_END),
                mandatory("PSOBKEY", 4, 15),
                mandatory("EINNAHMEART", 16, 20),
                mandatory("BETRW", 21, 31),
                mandatory("FAEDN", 32, 39),
                mandatory("BLDAT", 40, 47),
                column("PERSL", 48, 51),
                mandatory("XBLNR", 396, 407),
                mandatory("FV_BELNR", 408, 419),
                // BLWAE is not in the 210 record; Satzart210#blwae is stamped with SATZART210_BLWAE.
                trailing("KOSTL", 422, 431),
                trailing("MWSKZ", 444, 445),
                trailing("AUFNR", 432, 443)));
        layouts.put("250", Layout.ofColumns(
                mandatory("SATZART", 1, SATZART_END),
                mandatory("PSOBKEY", 4, 15),
                mandatory("EINNAHMEART", 16, 20),
                mandatory("BETRW", 73, 83),
                mandatory("FAEDN", 32, 39),
                mandatory("BLDAT", 65, 72),
                column("PERSL", 48, 51),
                column("OPTXT", 88, 137),
                column("SGTXT", 88, 137),
                column("BLWAE", 412, 413),
                mandatory("VALUT", 414, 421),
                mandatory("XBLNR", 422, 433),
                mandatory("FV_BELNR", 434, 445),
                column("KOSTL", 448, 457),
                column("MWSKZ", 470, 471),
                column("AUFNR", 458, 469)));
        layouts.put("260", Layout.ofColumns(
                mandatory("SATZART", 1, SATZART_END),
                mandatory("PSOBKEY", 4, 15),
                mandatory("EINNAHMEART", 16, 20),
                mandatory("BETRW", 21, 31),
                mandatory("FAEDN", 32, 39),
                mandatory("BLDAT", 40, 47),
                column("PERSL", 48, 51),
                mandatory("XBLNR", 396, 407),
                mandatory("FV_BELNR", 408, 419),
                trailing("KOSTL", 422, 431),
                trailing("MWSKZ", 444, 445),
                trailing("AUFNR", 432, 443)));
        // CPD-ON
        return Collections.unmodifiableMap(layouts);
    }

    /**
     * Package-private so the layout-consistency test asserts the same check {@link #parse} enforces.
     */
    /* default */ static List<String> inconsistentColumns() {
        return INCONSISTENT_COLUMNS;
    }

    /**
     * Check the transcribed positions for the little that is decidable without the spec at hand.
     *
     * <p>
     * Not much is: gaps are expected, because the record carries fields this integration does not
     * map, and <em>overlaps are legitimate too</em>: the spec feeds some domain fields from shared
     * columns (OPTXT and SGTXT of SATZART {@code 200} and {@code 250} are the same characters). That
     * leaves only positions that cannot exist. Everything else about the table's correctness rests on
     * the real sample batches, above all the checksum reconciliation in
     * {@code PscdSampleBatchChecksumTest}.
     * </p>
     */
    private static List<String> findInconsistentColumns() {
        final List<String> problems = new ArrayList<>();
        LAYOUTS.forEach((code, layout) -> problems.addAll(inconsistenciesIn(code, layout)));
        return List.copyOf(problems);
    }

    private static List<String> inconsistenciesIn(final String code, final Layout layout) {
        final List<String> problems = new ArrayList<>();
        for (final Column column : layout.columns()) {
            if (column.start() < 1) {
                problems.add("%s.%s starts at column %d".formatted(code, column.name(), column.start()));
            }
            if (column.end() < column.start()) {
                problems.add("%s.%s ends at column %d, before its start column %d"
                        .formatted(code, column.name(), column.end(), column.start()));
            }
        }
        return problems;
    }

    /**
     * One fixed-width column: the spec's field name and the 1-based columns it spans, {@code start}
     * and {@code end} both inclusive, copied verbatim from the record layout spec, so a field the
     * spec writes as "PSOBTXTB1 396-449" is entered as {@code new Column("PSOBTXTB1", 396, 449)} with
     * no arithmetic.
     *
     * <p>
     * {@link #fromIndex()} and {@link #toIndex()} are the only place the 1-based-inclusive range
     * becomes a 0-based half-open string index, so the off-by-one lives exactly once.
     * </p>
     */
    /* default */ record Column(String name, int start, int end, boolean trailing, boolean mandatory) {

        /* default */ int fromIndex() {
            return this.start - 1;
        }

        /* default */ int toIndex() {
            return this.end;
        }
    }

    /** A column every record of its type carries, and may leave empty. */
    private static Column column(final String name, final int start, final int end) {
        return new Column(name, start, end, false, false);
    }

    /**
     * A column {@link PscdSatzartenValidator} declares mandatory for this record type. Left empty by
     * the file, it is filled with {@value #REQUIRED_PLACEHOLDER} and named in the record's FEHLER
     * rather than failing the batch, the legacy behaviour described in the class Javadoc.
     *
     * <p>
     * Which columns carry this is asserted against the validator's own rules by
     * {@code PscdSatzartenParserTest}, so the two cannot drift apart.
     * </p>
     */
    private static Column mandatory(final String name, final int start, final int end) {
        return new Column(name, start, end, false, true);
    }

    /**
     * A column the record may stop short of. SATZART 210 and 260 arrive in two lengths, 419 and 445
     * characters, and the vendor WSDL declares the trailing KOSTL / AUFNR / MWSKZ block
     * {@code minOccurs="0"}, so a 419-character record is complete, not truncated, and those three
     * columns are simply not there.
     */
    private static Column trailing(final String name, final int start, final int end) {
        return new Column(name, start, end, true, false);
    }

    /**
     * The columns of one record type, with the line length they require: the end of the last mapped
     * column, <em>not</em> the record's total length. A record may carry fields this integration does
     * not map, so anything past that point is none of this parser's business.
     */
    /* default */ record Layout(List<Column> columns, int requiredLength) {

        private static Layout ofColumns(final Column... columns) {
            final List<Column> declared = List.of(columns);
            // Trailing columns are excluded on purpose: they are what a short record leaves off, so
            // requiring them would reject the shorter of the two legitimate 210/260 variants.
            return new Layout(declared, declared.stream()
                    .filter(c -> !c.trailing())
                    .mapToInt(Column::toIndex)
                    .max().orElse(0));
        }
    }

    /**
     * Sequential reader over one line's already-sliced columns.
     *
     * <p>
     * The record builders below take their fields from this in column order, so no hand-written
     * indices exist that could drift out of step with {@link #LAYOUTS}: the order of the builder
     * calls <em>is</em> the column order. {@link #requireExhausted} catches a builder and its layout
     * entry disagreeing on the field count, which would otherwise shift every value after the gap.
     * </p>
     */
    private static final class Fields {

        private final List<String> values;

        /** Mandatory columns this line left empty, in layout order, as their contract names. */
        private final List<String> filledIn;

        /** What was wrong with the line itself, from {@link #truncation}. */
        private final List<String> notes;

        private int position;

        private Fields(final List<String> values, final List<String> filledIn, final List<String> notes) {
            this.values = values;
            this.filledIn = filledIn;
            this.notes = notes;
        }

        /**
         * The FEHLER for this record: what was wrong with the line, and which mandatory columns were
         * filled with {@value PscdSatzartenParser#REQUIRED_PLACEHOLDER}, and {@code null} when the record
         * was complete. Read by every record builder, and outside the sequential cursor, because FEHLER is
         * not a column.
         */
        private String fehler() {
            final List<String> parts = new ArrayList<>(this.notes);
            if (!this.filledIn.isEmpty()) {
                parts.add(REQUIRED_PLACEHOLDER + ": " + String.join(", ", this.filledIn));
            }
            if (parts.isEmpty()) {
                return null;
            }
            return capped(String.join("; ", parts));
        }

        private String next() {
            if (this.position >= this.values.size()) {
                throw new IllegalStateException("Record builder reads more fields than its layout declares");
            }
            final String value = this.values.get(this.position);
            this.position++;
            return value;
        }

        private void requireExhausted(final String code) {
            if (this.position != this.values.size()) {
                throw new IllegalStateException("Record builder for SATZART %s read %d of %d declared columns"
                        .formatted(code, this.position, this.values.size()));
            }
        }
    }

    /**
     * Mutable accumulator for one batch: files each parsed record under its SATZART code while the
     * lines are walked, then assembles the immutable aggregate. Keeps the record-type dispatch a
     * self-contained unit instead of inflating {@link #parse}.
     */
    private static final class RecordCollector {

        private Satzart010 satzart010;
        private final List<Satzart100> satzart100 = new ArrayList<>();
        private final List<Satzart105> satzart105 = new ArrayList<>();
        private final List<Satzart155> satzart155 = new ArrayList<>();
        private final List<Satzart165> satzart165 = new ArrayList<>();
        private final List<Satzart200> satzart200 = new ArrayList<>();
        private final List<Satzart210> satzart210 = new ArrayList<>();
        private final List<Satzart250> satzart250 = new ArrayList<>();
        private final List<Satzart260> satzart260 = new ArrayList<>();

        /** Lines that could not be mapped to a record type, as the contract's own error records. */
        private final List<SatzartFehler> satzartFehler = new ArrayList<>();

        /**
         * Decode one sliced record and file it under its SATZART code.
         *
         * <p>
         * The mandatory-field check from {@link PscdSatzartenValidator} still runs, but on this channel it
         * has nothing left to find: every field it requires is a column the layout marks, and
         * {@link #slice} has already filled those with {@value #REQUIRED_PLACEHOLDER}, which is the legacy
         * behaviour the class Javadoc describes. Anything it does report means the layout table and the
         * validator have drifted apart, which {@code PscdSatzartenParserTest} is there to stop; it becomes
         * an accounting problem rather than a failed batch, like every other content defect on this
         * channel.
         * </p>
         */
        @SuppressWarnings("PMD.CyclomaticComplexity") // one branch per SATZART type: a flat dispatch, not tangled logic
        private void add(final String code, final Fields fields, final LineContext context) {
            switch (code) {
            case "010" -> {
                // Unlike the repeatable types below, the control record is a single slot: a second 010
                // record in the same batch overwrites the first.
                this.satzart010 = checked(toSatzart010(fields), PscdSatzartenValidator::missingMandatoryFields, code, context);
            }
            case "100" -> this.satzart100.add(checked(toSatzart100(fields), PscdSatzartenValidator::missingMandatoryFields, code, context));
            case "105" -> this.satzart105.add(checked(toSatzart105(fields), PscdSatzartenValidator::missingMandatoryFields, code, context));
            case "155" -> this.satzart155.add(checked(toSatzart155(fields), PscdSatzartenValidator::missingMandatoryFields, code, context));
            case "165" -> this.satzart165.add(checked(toSatzart165(fields), PscdSatzartenValidator::missingMandatoryFields, code, context));
            case "200" -> this.satzart200.add(checked(toSatzart200(fields), PscdSatzartenValidator::missingMandatoryFields, code, context));
            case "210" -> this.satzart210.add(checked(toSatzart210(fields), PscdSatzartenValidator::missingMandatoryFields, code, context));
            case "250" -> this.satzart250.add(checked(toSatzart250(fields), PscdSatzartenValidator::missingMandatoryFields, code, context));
            case "260" -> this.satzart260.add(checked(toSatzart260(fields), PscdSatzartenValidator::missingMandatoryFields, code, context));
            default -> throw new IllegalStateException("SATZART code '" + code + "' has a layout but no record builder");
            }
            fields.requireExhausted(code);
        }

        /** Note a problem for every mandatory field the decoded record left blank, and return it. */
        private static <T> T checked(final T record, final Function<T, List<String>> mandatoryFields,
                final String code, final LineContext context) {
            final List<String> missing = mandatoryFields.apply(record);
            if (!missing.isEmpty()) {
                context.problem("SATZART %s is missing %s".formatted(code, String.join(", ", missing)));
            }
            return record;
        }

        /**
         * File a line that could not be mapped to a record type as a standalone error record, so the
         * batch still carries it and PSCD is told what was wrong. The line number is part of the text:
         * it is what lets someone find the offending line in the original file.
         *
         * @param satzart the line's SATZART code, or {@value #UNREADABLE_SATZART} when it had none
         * @param detail what was wrong, as it goes into FEHLERTEXT
         */
        private void addUnmappable(final String satzart, final String detail, final LineContext context) {
            context.problem(detail);
            this.satzartFehler.add(SatzartFehler.builder()
                    .satzart(satzart)
                    .fehlertext(capped("line %d: %s".formatted(context.lineNumber(), detail)))
                    .build());
        }

        private PscdSatzarten toAggregate(final String filename) {
            return PscdSatzarten.builder()
                    .filename(filename)
                    .satzart010(this.satzart010)
                    .satzart100(this.satzart100)
                    .satzart105(this.satzart105)
                    .satzart155(this.satzart155)
                    .satzart165(this.satzart165)
                    .satzart200(this.satzart200)
                    .satzart210(this.satzart210)
                    .satzart250(this.satzart250)
                    .satzart260(this.satzart260)
                    // Empty unless a line could not be mapped to a record type at all: PSCD normally
                    // fills these on the way back, and this channel borrows the shape to report a line
                    // it could not decode rather than dropping it.
                    .satzartFehler(this.satzartFehler)
                    .build();
        }
    }

    // CPD-OFF - the per-record field mappings are intentionally repetitive.
    // Each reads its fields in column order; FEHLER is absent from every layout, so no record sets it.
    private static Satzart010 toSatzart010(final Fields f) {
        return Satzart010.builder()
                .satzart(f.next())
                .abstimmsumme(f.next())
                .vorzeichen(f.next())
                .fehler(f.fehler())
                .build();
    }

    private static Satzart100 toSatzart100(final Fields f) {
        return Satzart100.builder()
                .satzart(f.next())
                .psobkey(f.next())
                .partner(f.next())
                .addrnum(f.next())
                .psobtxtb1(f.next())
                .psobtxtb2(f.next())
                .psobtxtb3(f.next())
                .psobtxtb4(f.next())
                .psobtxtb5(f.next())
                .psobtxtb6(f.next())
                .zweitschuldner(f.next())
                .eigentuemerwechsel(f.next())
                .betriebsende(f.next())
                .kdKenn(f.next())
                .fachdstSb(f.next())
                .fachdstTelnr(f.next())
                .fehler(f.fehler())
                .build();
    }

    private static Satzart105 toSatzart105(final Fields f) {
        return Satzart105.builder()
                .satzart(f.next())
                .psobkey(f.next())
                .corrPartner(f.next())
                .corrRole(f.next())
                .fehler(f.fehler())
                .build();
    }

    private static Satzart155 toSatzart155(final Fields f) {
        return Satzart155.builder()
                .satzart(f.next())
                .psobkey(f.next())
                .deleteFlag(f.next())
                .corrRole(f.next())
                .fehler(f.fehler())
                .build();
    }

    private static Satzart165 toSatzart165(final Fields f) {
        return Satzart165.builder()
                .satzart(f.next())
                .psobkey(f.next())
                .psobtxtb1(f.next())
                .psobtxtb2(f.next())
                .psobtxtb3(f.next())
                .psobtxtb4(f.next())
                .psobtxtb5(f.next())
                .psobtxtb6(f.next())
                .zweitschuldner(f.next())
                .eigentuemerwechsel(f.next())
                .betriebsende(f.next())
                .kdKenn(f.next())
                .fachdstSb(f.next())
                .fachdstTelnr(f.next())
                .fehler(f.fehler())
                .build();
    }

    private static Satzart200 toSatzart200(final Fields f) {
        return Satzart200.builder()
                .satzart(f.next())
                .psobkey(f.next())
                .einnahmeart(f.next())
                .betrw(f.next())
                .faedn(f.next())
                .bldat(f.next())
                .persl(f.next())
                .optxt(f.next())
                .sgtxt(f.next())
                .blwae(f.next())
                .xblnr(f.next())
                .fvBelnr(f.next())
                .kostl(f.next())
                .mwskz(f.next())
                .aufnr(f.next())
                .fehler(f.fehler())
                .build();
    }

    private static Satzart210 toSatzart210(final Fields f) {
        return Satzart210.builder()
                .satzart(f.next())
                .psobkey(f.next())
                .einnahmeart(f.next())
                .betrw(f.next())
                .faedn(f.next())
                .bldat(f.next())
                .persl(f.next())
                .xblnr(f.next())
                .fvBelnr(f.next())
                // Not a column: the 210 record has no BLWAE field.
                .blwae(SATZART210_BLWAE)
                .kostl(f.next())
                .mwskz(f.next())
                .aufnr(f.next())
                .fehler(f.fehler())
                .build();
    }

    private static Satzart250 toSatzart250(final Fields f) {
        return Satzart250.builder()
                .satzart(f.next())
                .psobkey(f.next())
                .einnahmeart(f.next())
                .betrw(f.next())
                .faedn(f.next())
                .bldat(f.next())
                .persl(f.next())
                .optxt(f.next())
                .sgtxt(f.next())
                .blwae(f.next())
                .valut(f.next())
                .xblnr(f.next())
                .fvBelnr(f.next())
                .kostl(f.next())
                .mwskz(f.next())
                .aufnr(f.next())
                .fehler(f.fehler())
                .build();
    }

    private static Satzart260 toSatzart260(final Fields f) {
        return Satzart260.builder()
                .satzart(f.next())
                .psobkey(f.next())
                .einnahmeart(f.next())
                .betrw(f.next())
                .faedn(f.next())
                .bldat(f.next())
                .persl(f.next())
                .xblnr(f.next())
                .fvBelnr(f.next())
                .kostl(f.next())
                .mwskz(f.next())
                .aufnr(f.next())
                .fehler(f.fehler())
                .build();
    }
    // CPD-ON

}
