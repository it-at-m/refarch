package de.muenchen.oss.refarch.integration.pscd.domain.validation;

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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Which fields a PSCD record must carry, for every inbound channel.
 *
 * <p>
 * This is a contract invariant rather than a transport detail, so it lives in the domain and is the
 * single definition all three channels use: the flat-file parser applies it per line (adding the
 * line
 * number, which only it knows), and the canonical mapper applies it to a SOAP or REST payload. A
 * record that reaches {@code PscdOutPort} has passed the same check whichever way it arrived.
 * </p>
 *
 * <p>
 * The baseline is the vendor WSDL's {@code minOccurs}, with one deliberate departure, on SATZART
 * {@code 250} and only there: {@code VALUT} is required here although the WSDL declares it
 * {@code minOccurs="0"}. The record layout marks that column mandatory, and all 304 SATZART 250
 * records of the complete sample batch carry a value date, so one arriving without it is a defect
 * worth reporting rather than something to pass on. That makes SOAP and REST stricter here than the
 * vendor schema alone: a 250 without VALUT is rejected. See
 * {@link #missingMandatoryFields(Satzart250)}.
 * </p>
 *
 * <p>
 * Beyond that, this list and the flat-file layout table in {@code PscdSatzartenParser} say the same
 * thing field for field, and {@code PscdSatzartenParserTest} holds them to it.
 * </p>
 *
 * <p>
 * Names reported are the contract's own ({@code PSOBKEY}, {@code FV_BELNR}), not the Java field
 * names, so a message can be matched against the record layout spec without translation.
 * </p>
 */
public final class PscdSatzartenValidator {

    private static final String SATZART = "SATZART";
    private static final String ABSTIMMSUMME = "ABSTIMMSUMME";
    private static final String VORZEICHEN = "VORZEICHEN";
    private static final String PSOBKEY = "PSOBKEY";
    private static final String PARTNER = "PARTNER";
    private static final String CORR_PARTNER = "CORR_PARTNER";
    private static final String CORR_ROLE = "CORR_ROLE";
    private static final String EINNAHMEART = "EINNAHMEART";
    private static final String BETRW = "BETRW";
    private static final String FAEDN = "FAEDN";
    private static final String BLDAT = "BLDAT";
    private static final String XBLNR = "XBLNR";
    private static final String FV_BELNR = "FV_BELNR";
    private static final String VALUT = "VALUT";

    private PscdSatzartenValidator() {
    }

    /**
     * Check every record of a batch and reject it as a whole if any field is missing.
     *
     * <p>
     * Every violation in the payload is reported, not just the first: a caller that gets this back as
     * a {@code 400} should be able to fix the lot in one pass.
     * </p>
     *
     * @param batch the batch to check (may be {@code null}, which is not this method's concern)
     * @throws PscdValidationException if any record is missing a mandatory field
     */
    public static void requireMandatoryFields(final PscdSatzarten batch) {
        if (batch == null) {
            return;
        }
        final List<String> violations = new ArrayList<>();
        record(violations, "Satzart010", batch.getSatzart010() == null ? List.of() : List.of(batch.getSatzart010()),
                PscdSatzartenValidator::missingMandatoryFields);
        record(violations, "Satzart100", batch.getSatzart100(), PscdSatzartenValidator::missingMandatoryFields);
        record(violations, "Satzart105", batch.getSatzart105(), PscdSatzartenValidator::missingMandatoryFields);
        record(violations, "Satzart155", batch.getSatzart155(), PscdSatzartenValidator::missingMandatoryFields);
        record(violations, "Satzart165", batch.getSatzart165(), PscdSatzartenValidator::missingMandatoryFields);
        record(violations, "Satzart200", batch.getSatzart200(), PscdSatzartenValidator::missingMandatoryFields);
        record(violations, "Satzart210", batch.getSatzart210(), PscdSatzartenValidator::missingMandatoryFields);
        record(violations, "Satzart250", batch.getSatzart250(), PscdSatzartenValidator::missingMandatoryFields);
        record(violations, "Satzart260", batch.getSatzart260(), PscdSatzartenValidator::missingMandatoryFields);
        if (!violations.isEmpty()) {
            throw new PscdValidationException("PSCD batch has %d record(s) missing mandatory fields: %s"
                    .formatted(violations.size(), String.join("; ", violations)));
        }
    }

    /** Append {@code <type>[<index>]: missing A, B} for every record of one type that fails. */
    private static <T> void record(final List<String> violations, final String type, final List<T> records,
            final Function<T, List<String>> check) {
        if (records == null) {
            return;
        }
        for (int i = 0; i < records.size(); i++) {
            final List<String> missing = check.apply(records.get(i));
            if (!missing.isEmpty()) {
                violations.add("%s[%d]: missing %s".formatted(type, i, String.join(", ", missing)));
            }
        }
    }

    // CPD-OFF - one rule list per SATZART type; near-identical by design and meant to be read against
    // the record layout spec side by side.
    public static List<String> missingMandatoryFields(final Satzart010 satzart) {
        final List<String> missing = new ArrayList<>();
        require(missing, SATZART, satzart.getSatzart());
        require(missing, ABSTIMMSUMME, satzart.getAbstimmsumme());
        require(missing, VORZEICHEN, satzart.getVorzeichen());
        return missing;
    }

    public static List<String> missingMandatoryFields(final Satzart100 satzart) {
        final List<String> missing = new ArrayList<>();
        require(missing, SATZART, satzart.getSatzart());
        require(missing, PSOBKEY, satzart.getPsobkey());
        require(missing, PARTNER, satzart.getPartner());
        return missing;
    }

    public static List<String> missingMandatoryFields(final Satzart105 satzart) {
        final List<String> missing = new ArrayList<>();
        require(missing, SATZART, satzart.getSatzart());
        require(missing, PSOBKEY, satzart.getPsobkey());
        require(missing, CORR_PARTNER, satzart.getCorrPartner());
        require(missing, CORR_ROLE, satzart.getCorrRole());
        return missing;
    }

    public static List<String> missingMandatoryFields(final Satzart155 satzart) {
        final List<String> missing = new ArrayList<>();
        require(missing, SATZART, satzart.getSatzart());
        require(missing, PSOBKEY, satzart.getPsobkey());
        return missing;
    }

    public static List<String> missingMandatoryFields(final Satzart165 satzart) {
        final List<String> missing = new ArrayList<>();
        require(missing, SATZART, satzart.getSatzart());
        require(missing, PSOBKEY, satzart.getPsobkey());
        return missing;
    }

    public static List<String> missingMandatoryFields(final Satzart200 satzart) {
        final List<String> missing = new ArrayList<>();
        require(missing, SATZART, satzart.getSatzart());
        require(missing, PSOBKEY, satzart.getPsobkey());
        require(missing, EINNAHMEART, satzart.getEinnahmeart());
        require(missing, BETRW, satzart.getBetrw());
        require(missing, FAEDN, satzart.getFaedn());
        require(missing, BLDAT, satzart.getBldat());
        require(missing, XBLNR, satzart.getXblnr());
        require(missing, FV_BELNR, satzart.getFvBelnr());
        return missing;
    }

    public static List<String> missingMandatoryFields(final Satzart210 satzart) {
        final List<String> missing = new ArrayList<>();
        require(missing, SATZART, satzart.getSatzart());
        require(missing, PSOBKEY, satzart.getPsobkey());
        require(missing, EINNAHMEART, satzart.getEinnahmeart());
        require(missing, BETRW, satzart.getBetrw());
        require(missing, FAEDN, satzart.getFaedn());
        require(missing, BLDAT, satzart.getBldat());
        require(missing, XBLNR, satzart.getXblnr());
        require(missing, FV_BELNR, satzart.getFvBelnr());
        return missing;
    }

    public static List<String> missingMandatoryFields(final Satzart250 satzart) {
        final List<String> missing = new ArrayList<>();
        require(missing, SATZART, satzart.getSatzart());
        require(missing, PSOBKEY, satzart.getPsobkey());
        require(missing, EINNAHMEART, satzart.getEinnahmeart());
        require(missing, BETRW, satzart.getBetrw());
        require(missing, FAEDN, satzart.getFaedn());
        require(missing, BLDAT, satzart.getBldat());
        require(missing, VALUT, satzart.getValut());
        require(missing, XBLNR, satzart.getXblnr());
        require(missing, FV_BELNR, satzart.getFvBelnr());
        return missing;
    }

    public static List<String> missingMandatoryFields(final Satzart260 satzart) {
        final List<String> missing = new ArrayList<>();
        require(missing, SATZART, satzart.getSatzart());
        require(missing, PSOBKEY, satzart.getPsobkey());
        require(missing, EINNAHMEART, satzart.getEinnahmeart());
        require(missing, BETRW, satzart.getBetrw());
        require(missing, FAEDN, satzart.getFaedn());
        require(missing, BLDAT, satzart.getBldat());
        require(missing, XBLNR, satzart.getXblnr());
        require(missing, FV_BELNR, satzart.getFvBelnr());
        return missing;
    }
    // CPD-ON

    private static void require(final List<String> missing, final String name, final String value) {
        if (value == null || value.isBlank()) {
            missing.add(name);
        }
    }
}
