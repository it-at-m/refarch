package de.muenchen.oss.refarch.integration.pscd.service.adapter.in.file;

import de.muenchen.oss.refarch.integration.pscd.service.adapter.in.file.PscdSatzartenParser.Column;
import de.muenchen.oss.refarch.integration.pscd.service.adapter.in.file.PscdSatzartenParser.Layout;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds fixed-width PSCD record lines for tests, from the parser's own column layout.
 *
 * <p>
 * Fixtures built here survive a correction to the column widths, which is what keeps the behaviour
 * tests about behaviour. They cannot, by construction, catch a <em>wrong</em> layout table;
 * {@code PscdSampleBatchChecksumTest} is the cross-check for that, reconciling the complete sample
 * batch against the checksum the predecessor put in it.
 * </p>
 *
 * <p>
 * Lives in the parser's package because {@code PscdSatzartenParser.layoutsByCode()} is
 * package-private,
 * and is public so the service-level file-channel test can use it too.
 * </p>
 */
public final class PscdRecordFixtures {

    private PscdRecordFixtures() {
    }

    /**
     * Columns transcribed to impossible or overlapping positions. Delegates to the parser so the test
     * asserts the very check {@code parse} enforces, rather than a second implementation of it.
     */
    public static List<String> inconsistentColumns() {
        return PscdSatzartenParser.inconsistentColumns();
    }

    /**
     * Build one record line by writing each named value at its own column into an otherwise blank
     * buffer. Columns not named in {@code fields} stay blank, as do the regions between them: the
     * record's unmapped fields, which this parser never reads. SATZART is filled from {@code code}.
     *
     * <p>
     * Values are left-aligned within their column; the parser strips either way, so a right-aligned
     * spec needs no change here.
     * </p>
     *
     * <p>
     * Where two columns share characters (OPTXT and SGTXT do, by spec) they write into the same
     * region and the later declaration wins. Name only one of such a pair, or both fields will come
     * back holding whichever value was written last.
     * </p>
     */
    public static String line(final String code, final Map<String, String> fields) {
        final Layout layout = PscdSatzartenParser.layoutsByCode().get(code);
        if (layout == null) {
            throw new IllegalArgumentException("No layout for SATZART code '" + code + '\'');
        }
        final int length = lineLength(layout, fields);
        final char[] buffer = new char[length];
        Arrays.fill(buffer, ' ');
        for (final Column column : layout.columns()) {
            final String value = "SATZART".equals(column.name()) ? code : fields.getOrDefault(column.name(), "");
            final int width = column.toIndex() - column.fromIndex();
            if (value.length() > width) {
                throw new IllegalArgumentException("Test value '%s' does not fit the %d-character column %s"
                        .formatted(value, width, column.name()));
            }
            if (column.toIndex() <= length) {
                value.getChars(0, value.length(), buffer, column.fromIndex());
            }
        }
        return new String(buffer);
    }

    /**
     * Like {@link #line}, but every non-trailing column the caller did not name is filled with a
     * placeholder, so the record satisfies its mandatory fields whatever they are.
     *
     * <p>
     * Positive tests use this and override only what they assert on, which keeps them from breaking
     * every time a field becomes mandatory. A test that wants a field <em>missing</em> uses
     * {@link #line} instead. Trailing columns are deliberately left to the caller, so naming
     * KOSTL/AUFNR/MWSKZ still selects the long 210/260 variant and omitting them the short one.
     * </p>
     */
    public static String validLine(final String code, final Map<String, String> overrides) {
        final Layout layout = PscdSatzartenParser.layoutsByCode().get(code);
        if (layout == null) {
            throw new IllegalArgumentException("No layout for SATZART code '" + code + '\'');
        }
        final Map<String, String> fields = new LinkedHashMap<>();
        for (final Column column : layout.columns()) {
            if (!column.trailing()) {
                fields.put(column.name(), "X");
            }
        }
        fields.putAll(overrides);
        return line(code, fields);
    }

    /**
     * How long the built line should be: its record's required length, extended only to cover a
     * trailing column the caller actually named. So naming KOSTL/AUFNR/MWSKZ yields the long 210/260
     * variant and leaving them out yields the short one, without a test ever spelling out a length.
     */
    private static int lineLength(final Layout layout, final Map<String, String> fields) {
        int length = layout.requiredLength();
        for (final Column column : layout.columns()) {
            if (column.trailing() && fields.containsKey(column.name())) {
                length = Math.max(length, column.toIndex());
            }
        }
        return length;
    }

    /** Length of the leading SATZART column, for fixtures that overwrite the record code. */
    public static int satzartLength() {
        final Column satzart = PscdSatzartenParser.layoutsByCode().get("100").columns().get(0);
        return satzart.toIndex() - satzart.fromIndex();
    }
}
