package de.muenchen.oss.refarch.integration.pscd.service.adapter.in.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import de.muenchen.oss.refarch.integration.pscd.domain.validation.PscdSatzartenValidator;
import de.muenchen.oss.refarch.integration.pscd.service.adapter.in.file.PscdSatzartenParser.Column;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

// SATZART codes and column names repeat across the fixtures by nature; spelling them out is what
// makes each fixture readable as a record.
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class PscdSatzartenParserTest {

    @Test
    void everyRecordTypeHasALayout() {
        assertThat(PscdSatzartenParser.layoutsByCode()).containsOnlyKeys("010", "100", "105", "155", "165", "200", "210", "250", "260");
    }

    /**
     * Very little about the table is checkable without the spec: gaps are expected (unmapped fields)
     * and so are overlaps (OPTXT and SGTXT share their columns by spec). Only positions that cannot
     * exist are left; {@link PscdSampleBatchChecksumTest} is what really pins the table, against real
     * data.
     */
    @Test
    void layoutColumnsAreWithinBounds() {
        assertThat(PscdRecordFixtures.inconsistentColumns())
                .as("columns transcribed to impossible positions")
                .isEmpty();
    }

    /**
     * The layout marks which columns are mandatory, so that an empty one can be filled with
     * {@code REQUIRED} instead of failing the batch, and that marking must say exactly what
     * {@link PscdSatzartenValidator} requires, with no exception on any record type. Two places, one
     * rule: this is what stops them drifting unnoticed, in either direction. A column marked here but
     * not required there would be repaired on no authority; a field required there but not marked here
     * would leave the file channel reporting a gap it could have filled, and the batch carrying it.
     */
    @Test
    void theMandatoryColumnsAreExactlyWhatTheValidatorRequires() {
        final Map<String, List<String>> required = new LinkedHashMap<>();
        required.put("010", PscdSatzartenValidator.missingMandatoryFields(Satzart010.builder().build()));
        required.put("100", PscdSatzartenValidator.missingMandatoryFields(Satzart100.builder().build()));
        required.put("105", PscdSatzartenValidator.missingMandatoryFields(Satzart105.builder().build()));
        required.put("155", PscdSatzartenValidator.missingMandatoryFields(Satzart155.builder().build()));
        required.put("165", PscdSatzartenValidator.missingMandatoryFields(Satzart165.builder().build()));
        required.put("200", PscdSatzartenValidator.missingMandatoryFields(Satzart200.builder().build()));
        required.put("210", PscdSatzartenValidator.missingMandatoryFields(Satzart210.builder().build()));
        required.put("250", PscdSatzartenValidator.missingMandatoryFields(Satzart250.builder().build()));
        required.put("260", PscdSatzartenValidator.missingMandatoryFields(Satzart260.builder().build()));

        for (final Map.Entry<String, List<String>> rule : required.entrySet()) {
            final String code = rule.getKey();
            assertThat(mandatoryColumns(code))
                    .as("columns marked mandatory in the SATZART %s layout", code)
                    .containsExactlyInAnyOrderElementsOf(rule.getValue());
        }
    }

    private static List<String> mandatoryColumns(final String code) {
        return PscdSatzartenParser.layoutsByCode().get(code).columns().stream()
                .filter(Column::mandatory)
                .map(Column::name)
                .toList();
    }

    @Test
    void noLayoutCarriesTheReceivingSidesFehlerColumn() {
        assertThat(PscdSatzartenParser.layoutsByCode().values())
                .allSatisfy(layout -> assertThat(layout.columns()).extracting(Column::name).doesNotContain("FEHLER"));
    }

    @Nested
    class Parsing {

        @Test
        void parsesEachRecordTypeIntoTheAggregate() {
            final PscdSatzarten batch = PscdSatzartenParser.parse(List.of(
                    line("010", Map.of("ABSTIMMSUMME", "1234", "VORZEICHEN", "+")),
                    line("100", Map.of("PSOBKEY", "KEY1", "PARTNER", "PA1", "PSOBTXTB1", "TXT1")),
                    line("105", Map.of("PSOBKEY", "KEY1", "CORR_PARTNER", "CP", "CORR_ROLE", "RL")),
                    validLine("200", Map.of("PSOBKEY", "KEY2", "EINNAHMEART", "EA", "BETRW", "100", "XBLNR", "INV1", "FV_BELNR", "FV1"))),
                    "records.dat");

            assertThat(batch.getFilename()).isEqualTo("records.dat");
            assertThat(batch.getSatzart010().getAbstimmsumme()).isEqualTo("1234");
            assertThat(batch.getSatzart010().getVorzeichen()).isEqualTo("+");
            assertThat(batch.getSatzart100()).singleElement()
                    .satisfies(record -> {
                        assertThat(record.getSatzart()).isEqualTo("100");
                        assertThat(record.getPsobkey()).isEqualTo("KEY1");
                        assertThat(record.getPartner()).isEqualTo("PA1");
                        assertThat(record.getPsobtxtb1()).isEqualTo("TXT1");
                    });
            assertThat(batch.getSatzart105()).singleElement()
                    .satisfies(record -> assertThat(record.getCorrRole()).isEqualTo("RL"));
            assertThat(batch.getSatzart200()).singleElement()
                    .extracting(Satzart200::getXblnr, Satzart200::getFvBelnr)
                    .containsExactly("INV1", "FV1");
        }

        /**
         * Overlapping columns are legitimate: SATZART 200 feeds both OPTXT and SGTXT from the same
         * characters, so the same text must land in both domain fields. Guards against anyone
         * reinstating an overlap check that would reject the spec's own layout.
         */
        @Test
        void readsTwoDomainFieldsFromOneSharedColumnRange() {
            final Column optxt = columnOf("200", "OPTXT");
            final Column sgtxt = columnOf("200", "SGTXT");
            Assumptions.assumeTrue(optxt.start() == sgtxt.start() && optxt.end() == sgtxt.end(),
                    "OPTXT and SGTXT no longer share their columns in the transcribed layout");

            final PscdSatzarten batch = PscdSatzartenParser.parse(List.of(
                    // Spelled out rather than built with validLine: that fills every column, and
                    // SGTXT is declared after OPTXT, so its placeholder would overwrite the very
                    // characters this test writes.
                    line("200", Map.of("PSOBKEY", "KEY1", "EINNAHMEART", "EA", "BETRW", "100", "FAEDN", "20260101",
                            "BLDAT", "20260101", "XBLNR", "INV1", "FV_BELNR", "FV1", "OPTXT", "TEXT"))),
                    "f");

            assertThat(batch.getSatzart200()).singleElement()
                    .satisfies(record -> {
                        assertThat(record.getOptxt()).isEqualTo("TEXT");
                        assertThat(record.getSgtxt()).isEqualTo("TEXT");
                    });
        }

        private Column columnOf(final String code, final String name) {
            return PscdSatzartenParser.layoutsByCode().get(code).columns().stream()
                    .filter(column -> name.equals(column.name()))
                    .findFirst()
                    .orElseThrow();
        }

        /**
         * The 210 record has no BLWAE column, so the currency is stamped rather than read: the one
         * field on this channel that does not come from the line.
         */
        @Test
        void stampsTheFixedCurrencyOnSatzart210() {
            final PscdSatzarten batch = PscdSatzartenParser.parse(
                    List.of(validLine("210", Map.of("PSOBKEY", "KEY1", "EINNAHMEART", "EA", "BETRW", "100"))), "f");

            assertThat(batch.getSatzart210()).singleElement()
                    .satisfies(record -> {
                        assertThat(record.getBlwae()).isEqualTo("EUR");
                        assertThat(record.getPsobkey()).isEqualTo("KEY1");
                        assertThat(record.getBetrw()).isEqualTo("100");
                    });
        }

        @Test
        void noLayoutDeclaresABlwaeColumnForSatzart210() {
            assertThat(PscdSatzartenParser.layoutsByCode().get("210").columns())
                    .extracting(Column::name)
                    .doesNotContain("BLWAE");
        }

        /**
         * PERSL is optional on SATZART 210: the vendor WSDL declares it {@code minOccurs="0"}, and 3
         * of the 58 210 records in the sample batches leave it blank.
         */
        @Test
        void acceptsSatzart210WithoutPersl() {
            final PscdSatzarten batch = PscdSatzartenParser.parse(
                    List.of(validLine("210", Map.of("PSOBKEY", "KEY1", "PERSL", ""))), "f");

            assertThat(batch.getSatzart210()).singleElement()
                    .satisfies(record -> {
                        assertThat(record.getPsobkey()).isEqualTo("KEY1");
                        assertThat(record.getPersl()).isNull();
                    });
        }

        /**
         * CORR_ROLE is optional on SATZART 155: the vendor WSDL declares it {@code minOccurs="0"}
         * there (unlike on 105), and the sample batches' 155 records leave it blank.
         */
        @Test
        void acceptsSatzart155WithoutCorrRole() {
            final PscdSatzarten batch = PscdSatzartenParser.parse(
                    List.of(line("155", Map.of("PSOBKEY", "KEY1", "DELETE_FLAG", "X"))), "f");

            assertThat(batch.getSatzart155()).singleElement()
                    .satisfies(record -> {
                        assertThat(record.getPsobkey()).isEqualTo("KEY1");
                        assertThat(record.getDeleteFlag()).isEqualTo("X");
                        assertThat(record.getCorrRole()).isNull();
                    });
        }

        @Test
        void leavesBlankColumnsUnset() {
            final PscdSatzarten batch = PscdSatzartenParser.parse(
                    List.of(line("100", Map.of("PSOBKEY", "KEY1", "PARTNER", "PA1"))), "f");

            final Satzart100 record = batch.getSatzart100().get(0);
            assertThat(record.getSatzart()).isEqualTo("100");
            assertThat(record.getAddrnum()).isNull();
            assertThat(record.getFachdstTelnr()).isNull();
        }

        @Test
        void neverFillsFehlerNorStandaloneErrorRecords() {
            final PscdSatzarten batch = PscdSatzartenParser.parse(List.of(
                    validLine("100", Map.of("PSOBKEY", "KEY1", "PARTNER", "PA1")),
                    validLine("200", Map.of("PSOBKEY", "KEY1", "EINNAHMEART", "EA", "BETRW", "1"))),
                    "f");

            assertThat(batch.getSatzart100().get(0).getFehler()).isNull();
            assertThat(batch.getSatzart200().get(0).getFehler()).isNull();
            assertThat(batch.getSatzartFehler()).isEmpty();
        }

        /**
         * A record carries fields this integration does not map, so whatever follows the last mapped
         * column, padding or a real field we do not consume, is none of the parser's business.
         */
        @Test
        void ignoresEverythingPastTheLastMappedColumn() {
            final String record = line("010", Map.of("ABSTIMMSUMME", "1234", "VORZEICHEN", "+"));

            final PscdSatzarten padded = PscdSatzartenParser.parse(List.of(record + "   "), "f");
            final PscdSatzarten trailingField = PscdSatzartenParser.parse(List.of(record + "UNMAPPED-20260101"), "f");

            assertThat(padded.getSatzart010().getAbstimmsumme()).isEqualTo("1234");
            assertThat(trailingField.getSatzart010().getAbstimmsumme()).isEqualTo("1234");
            assertThat(trailingField.getSatzart010().getVorzeichen()).isEqualTo("+");
        }

        /**
         * The file channel carries ABSTIMMSUMME zero-padded to its column width. {@code Satzart010}
         * drops that padding as the record is built, so the batch, and the mandatory-field check the record
         * has just been through, carries the plain number, and an all-zero checksum survives as {@code 0}
         * instead of emptying the mandatory field and failing the batch.
         */
        @Test
        void dropsTheZeroPaddingOffAbstimmsumme() {
            final PscdSatzarten batch = PscdSatzartenParser.parse(
                    List.of(line("010", Map.of("ABSTIMMSUMME", "001234", "VORZEICHEN", "+"))), "f");

            assertThat(batch.getSatzart010().getAbstimmsumme()).isEqualTo("1234");
        }

        @Test
        void acceptsAnAllZeroAbstimmsummeAndKeepsItAsASingleZero() {
            final PscdSatzarten batch = PscdSatzartenParser.parse(
                    List.of(line("010", Map.of("ABSTIMMSUMME", "000000", "VORZEICHEN", "+"))), "f");

            assertThat(batch.getSatzart010().getAbstimmsumme()).isEqualTo("0");
        }

        /**
         * Legacy behaviour, kept deliberately: a mandatory column the file leaves empty does not fail
         * the batch. The field is filled with {@code REQUIRED} and the record's FEHLER says which
         * columns that happened to, so PSCD receives the batch and decides.
         */
        @Test
        void fillsAMissingMandatoryFieldWithRequiredAndReportsItInFehler() {
            final PscdSatzarten batch = PscdSatzartenParser.parse(
                    List.of(line("010", Map.of("ABSTIMMSUMME", "1234"))), "f");

            assertThat(batch.getSatzart010().getAbstimmsumme()).isEqualTo("1234");
            assertThat(batch.getSatzart010().getVorzeichen()).isEqualTo("REQUIRED");
            assertThat(batch.getSatzart010().getFehler()).isEqualTo("REQUIRED: VORZEICHEN");
        }

        /** Every gap in one record is filled, and the FEHLER lists them in layout order. */
        @Test
        void reportsEveryMissingMandatoryFieldOfARecord() {
            final PscdSatzarten batch = PscdSatzartenParser.parse(
                    List.of(validLine("200", Map.of("PSOBKEY", "", "BETRW", "", "FV_BELNR", ""))), "f");

            assertThat(batch.getSatzart200()).singleElement().satisfies(record -> {
                assertThat(record.getPsobkey()).isEqualTo("REQUIRED");
                assertThat(record.getBetrw()).isEqualTo("REQUIRED");
                assertThat(record.getFvBelnr()).isEqualTo("REQUIRED");
                assertThat(record.getFehler()).isEqualTo("REQUIRED: PSOBKEY, BETRW, FV_BELNR");
            });
        }

        /** An optional column stays unset; only mandatory ones are filled in. */
        @Test
        void leavesAnEmptyOptionalColumnAloneAndTheRecordUnmarked() {
            final PscdSatzarten batch = PscdSatzartenParser.parse(
                    List.of(validLine("200", Map.of("PERSL", "", "OPTXT", ""))), "f");

            assertThat(batch.getSatzart200()).singleElement().satisfies(record -> {
                assertThat(record.getPersl()).isNull();
                assertThat(record.getFehler()).isNull();
            });
        }

        @Test
        void leavesFehlerUnsetOnACompleteRecord() {
            final PscdSatzarten batch = PscdSatzartenParser.parse(List.of(
                    line("010", Map.of("ABSTIMMSUMME", "1234", "VORZEICHEN", "+")),
                    validLine("100", Map.of("PSOBKEY", "KEY1", "PARTNER", "PA1"))),
                    "f");

            assertThat(batch.getSatzart010().getFehler()).isNull();
            assertThat(batch.getSatzart100()).singleElement()
                    .satisfies(record -> assertThat(record.getFehler()).isNull());
        }

        @Test
        void aSecondControlRecordOverwritesTheFirst() {
            final PscdSatzarten batch = PscdSatzartenParser.parse(List.of(
                    line("010", Map.of("ABSTIMMSUMME", "1", "VORZEICHEN", "+")),
                    line("010", Map.of("ABSTIMMSUMME", "2", "VORZEICHEN", "-"))),
                    "f");

            assertThat(batch.getSatzart010().getAbstimmsumme()).isEqualTo("2");
        }

        @Test
        void skipsBlankLinesAndHandlesNullInput() {
            assertThat(PscdSatzartenParser.parse(null, "f").getSatzart200()).isEmpty();

            final PscdSatzarten batch = PscdSatzartenParser.parse(List.of(
                    "", line("010", Map.of("ABSTIMMSUMME", "1", "VORZEICHEN", "+")), "   "), "f");

            assertThat(batch.getSatzart010()).isNotNull();
        }

        /**
         * The poller always passes the polled file's name, so this guards the parser's other callers:
         * the filename identifies the batch end to end and must be present on every channel.
         */
        @Test
        void rejectsABatchMissingTheMandatoryFilename() {
            final List<String> lines = List.of(line("010", Map.of("ABSTIMMSUMME", "1", "VORZEICHEN", "+")));

            assertThatThrownBy(() -> PscdSatzartenParser.parse(lines, null))
                    .isInstanceOf(PscdValidationException.class)
                    .hasMessageContaining("filename");
            assertThatThrownBy(() -> PscdSatzartenParser.parse(lines, "   "))
                    .isInstanceOf(PscdValidationException.class)
                    .hasMessageContaining("filename");
        }

        /**
         * A line cut short is decoded as far as it reaches instead of failing the batch: the columns
         * beyond its end are empty, so the mandatory ones take {@code REQUIRED}, and the record says
         * both what was truncated and what had to be filled in.
         */
        @Test
        void marksALineTooShortForItsRecordLayoutInsteadOfRejectingIt() {
            final String valid = validLine("100", Map.of("PSOBKEY", "KEY1", "PARTNER", "PA1"));
            final String cutBeforePartner = valid.substring(0, 17);

            final PscdSatzarten batch = PscdSatzartenParser.parse(List.of("", cutBeforePartner), "batch.dat");

            assertThat(batch.getSatzart100()).singleElement().satisfies(record -> {
                assertThat(record.getPsobkey()).isEqualTo("KEY1");
                assertThat(record.getPartner()).isEqualTo("REQUIRED");
                assertThat(record.getFehler())
                        .contains("TRUNCATED: line is 17 of 821 characters")
                        .contains("REQUIRED: PARTNER");
            });
        }

        /**
         * A code with no layout cannot be sliced into any record type, so it travels as the contract's
         * own error record, carrying the line number, so the line can be found in the file.
         */
        @Test
        void reportsAnUnknownSatzartCodeAsAnErrorRecord() {
            final String unknown = withSatzartColumn("999", line("100", Map.of("PSOBKEY", "KEY1", "PARTNER", "PA1")));

            final PscdSatzarten batch = PscdSatzartenParser.parse(List.of(unknown), "batch.dat");

            assertThat(batch.getSatzart100()).isEmpty();
            assertThat(batch.getSatzartFehler()).singleElement().satisfies(fehler -> {
                assertThat(fehler.getSatzart()).isEqualTo("999");
                assertThat(fehler.getFehlertext())
                        .contains("line 1")
                        .contains("unknown SATZART code '999'");
            });
        }

        /** No code at all: the error record stands in for it, since SATZART is capped at 3 characters. */
        @Test
        void reportsABlankSatzartColumnAsAnErrorRecord() {
            final String blanked = withSatzartColumn("", line("100", Map.of("PSOBKEY", "KEY1", "PARTNER", "PA1")));

            final PscdSatzarten batch = PscdSatzartenParser.parse(List.of(blanked), "batch.dat");

            assertThat(batch.getSatzartFehler()).singleElement().satisfies(fehler -> {
                assertThat(fehler.getSatzart()).isEqualTo("???");
                assertThat(fehler.getFehlertext()).contains("SATZART column is blank");
            });
        }

        @Test
        void reportsALineTooShortToHoldASatzartCodeAsAnErrorRecord() {
            final PscdSatzarten batch = PscdSatzartenParser.parse(List.of("01"), "batch.dat");

            assertThat(batch.getSatzartFehler()).singleElement().satisfies(fehler -> {
                assertThat(fehler.getSatzart()).isEqualTo("???");
                assertThat(fehler.getFehlertext()).contains("too short to hold the 3-character SATZART column");
            });
        }

        /** Whatever else is in the file still arrives: one bad line no longer costs the batch. */
        @Test
        void deliversTheGoodRecordsAlongsideTheUnmappableOnes() {
            final PscdSatzarten batch = PscdSatzartenParser.parse(List.of(
                    line("010", Map.of("ABSTIMMSUMME", "1234", "VORZEICHEN", "+")),
                    withSatzartColumn("897", line("100", Map.of("PSOBKEY", "KEY1", "PARTNER", "PA1"))),
                    validLine("200", Map.of("PSOBKEY", "KEY2", "EINNAHMEART", "EA", "BETRW", "100"))),
                    "batch.dat");

            assertThat(batch.getSatzart010()).isNotNull();
            assertThat(batch.getSatzart200()).hasSize(1);
            assertThat(batch.getSatzartFehler()).singleElement()
                    .satisfies(fehler -> assertThat(fehler.getFehlertext()).contains("line 2"));
        }

        /** Overwrite the leading SATZART column of an otherwise valid line, leaving its length intact. */
        private String withSatzartColumn(final String code, final String line) {
            final int width = PscdRecordFixtures.satzartLength();
            return code + " ".repeat(width - code.length()) + line.substring(width);
        }
    }

    private static String line(final String code, final Map<String, String> fields) {
        return PscdRecordFixtures.line(code, fields);
    }

    /** A record whose mandatory fields are all populated; only what a test asserts on is named. */
    private static String validLine(final String code, final Map<String, String> fields) {
        return PscdRecordFixtures.validLine(code, fields);
    }
}
