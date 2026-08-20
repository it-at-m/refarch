package de.muenchen.oss.refarch.integration.pscd.service.adapter.in;

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
import de.muenchen.oss.refarch.integration.pscd.inbound.model.PscdBatch;
import de.muenchen.oss.refarch.integration.pscd.inbound.model.Satzart010Type;
import de.muenchen.oss.refarch.integration.pscd.inbound.model.Satzart100Type;
import de.muenchen.oss.refarch.integration.pscd.inbound.model.Satzart105Type;
import de.muenchen.oss.refarch.integration.pscd.inbound.model.Satzart155Type;
import de.muenchen.oss.refarch.integration.pscd.inbound.model.Satzart165Type;
import de.muenchen.oss.refarch.integration.pscd.inbound.model.Satzart200Type;
import de.muenchen.oss.refarch.integration.pscd.inbound.model.Satzart210Type;
import de.muenchen.oss.refarch.integration.pscd.inbound.model.Satzart250Type;
import de.muenchen.oss.refarch.integration.pscd.inbound.model.Satzart260Type;
import de.muenchen.oss.refarch.integration.pscd.inbound.model.SatzartFehlerType;

/**
 * Anti-corruption layer for the inbound edge: maps the generated canonical contract type
 * ({@link PscdBatch}) onto the {@link PscdSatzarten} domain aggregate.
 */
// High coupling and repetition are on purpose.
@SuppressWarnings("PMD.CouplingBetweenObjects")
public final class PscdInboundCanonicalMapper {

    private PscdInboundCanonicalMapper() {
    }

    // CPD-OFF - SATZART types share field blocks, so the per-record mappers are near-identical by design
    /**
     * Map a canonical batch onto the domain aggregate.
     *
     * @param source the deserialized canonical batch (from SOAP or REST)
     * @return the domain batch
     * @throws PscdValidationException if the payload is absent, has no filename, or is missing the
     *             mandatory control record
     */
    public static PscdSatzarten toDomain(final PscdBatch source) {
        if (source == null) {
            throw new PscdValidationException("PSCD batch payload is missing");
        }
        if (source.getFilename() == null || source.getFilename().isBlank()) {
            throw new PscdValidationException("PSCD batch is missing the mandatory filename");
        }
        if (source.getSatzart010() == null) {
            throw new PscdValidationException("PSCD batch is missing the mandatory Satzart010 record");
        }
        final PscdSatzarten batch = toBatch(source);
        // The same rule the file channel applies per line, so a record rejected there cannot slip in
        // through SOAP or REST instead. Reports every offending record, not just the first.
        PscdSatzartenValidator.requireMandatoryFields(batch);
        return batch;
    }

    private static PscdSatzarten toBatch(final PscdBatch source) {
        return PscdSatzarten.builder()
                .filename(source.getFilename())
                .satzart010(map(source.getSatzart010()))
                .satzart100(source.getSatzart100().stream().map(PscdInboundCanonicalMapper::map).toList())
                .satzart105(source.getSatzart105().stream().map(PscdInboundCanonicalMapper::map).toList())
                .satzart155(source.getSatzart155().stream().map(PscdInboundCanonicalMapper::map).toList())
                .satzart165(source.getSatzart165().stream().map(PscdInboundCanonicalMapper::map).toList())
                .satzart200(source.getSatzart200().stream().map(PscdInboundCanonicalMapper::map).toList())
                .satzart210(source.getSatzart210().stream().map(PscdInboundCanonicalMapper::map).toList())
                .satzart250(source.getSatzart250().stream().map(PscdInboundCanonicalMapper::map).toList())
                .satzart260(source.getSatzart260().stream().map(PscdInboundCanonicalMapper::map).toList())
                .satzartFehler(source.getSatzartFehler().stream().map(PscdInboundCanonicalMapper::map).toList())
                .build();
    }

    private static Satzart010 map(final Satzart010Type s) {
        return Satzart010.builder()
                .satzart(s.getSatzart())
                .abstimmsumme(s.getAbstimmsumme())
                .vorzeichen(s.getVorzeichen())
                .fehler(s.getFehler())
                .build();
    }

    private static Satzart100 map(final Satzart100Type s) {
        return Satzart100.builder()
                .satzart(s.getSatzart())
                .psobkey(s.getPsobkey())
                .partner(s.getPartner())
                .addrnum(s.getAddrnum())
                .psobtxtb1(s.getPsobtxtb1())
                .psobtxtb2(s.getPsobtxtb2())
                .psobtxtb3(s.getPsobtxtb3())
                .psobtxtb4(s.getPsobtxtb4())
                .psobtxtb5(s.getPsobtxtb5())
                .psobtxtb6(s.getPsobtxtb6())
                .zweitschuldner(s.getZweitschuldner())
                .eigentuemerwechsel(s.getEigentuemerwechsel())
                .betriebsende(s.getBetriebsende())
                .kdKenn(s.getKdKenn())
                .fachdstSb(s.getFachdstSb())
                .fachdstTelnr(s.getFachdstTelnr())
                .fehler(s.getFehler())
                .build();
    }

    private static Satzart105 map(final Satzart105Type s) {
        return Satzart105.builder()
                .satzart(s.getSatzart())
                .psobkey(s.getPsobkey())
                .corrPartner(s.getCorrPartner())
                .corrRole(s.getCorrRole())
                .fehler(s.getFehler())
                .build();
    }

    private static Satzart155 map(final Satzart155Type s) {
        return Satzart155.builder()
                .satzart(s.getSatzart())
                .psobkey(s.getPsobkey())
                .deleteFlag(s.getDeleteFlag())
                .corrRole(s.getCorrRole())
                .fehler(s.getFehler())
                .build();
    }

    private static Satzart165 map(final Satzart165Type s) {
        return Satzart165.builder()
                .satzart(s.getSatzart())
                .psobkey(s.getPsobkey())
                .psobtxtb1(s.getPsobtxtb1())
                .psobtxtb2(s.getPsobtxtb2())
                .psobtxtb3(s.getPsobtxtb3())
                .psobtxtb4(s.getPsobtxtb4())
                .psobtxtb5(s.getPsobtxtb5())
                .psobtxtb6(s.getPsobtxtb6())
                .zweitschuldner(s.getZweitschuldner())
                .eigentuemerwechsel(s.getEigentuemerwechsel())
                .betriebsende(s.getBetriebsende())
                .kdKenn(s.getKdKenn())
                .fachdstSb(s.getFachdstSb())
                .fachdstTelnr(s.getFachdstTelnr())
                .fehler(s.getFehler())
                .build();
    }

    private static Satzart200 map(final Satzart200Type s) {
        return Satzart200.builder()
                .satzart(s.getSatzart())
                .psobkey(s.getPsobkey())
                .einnahmeart(s.getEinnahmeart())
                .betrw(s.getBetrw())
                .faedn(s.getFaedn())
                .bldat(s.getBldat())
                .persl(s.getPersl())
                .optxt(s.getOptxt())
                .sgtxt(s.getSgtxt())
                .blwae(s.getBlwae())
                .xblnr(s.getXblnr())
                .fvBelnr(s.getFvBelnr())
                .kostl(s.getKostl())
                .fehler(s.getFehler())
                .mwskz(s.getMwskz())
                .aufnr(s.getAufnr())
                .build();
    }

    private static Satzart210 map(final Satzart210Type s) {
        return Satzart210.builder()
                .satzart(s.getSatzart())
                .psobkey(s.getPsobkey())
                .einnahmeart(s.getEinnahmeart())
                .betrw(s.getBetrw())
                .faedn(s.getFaedn())
                .bldat(s.getBldat())
                .persl(s.getPersl())
                .xblnr(s.getXblnr())
                .fvBelnr(s.getFvBelnr())
                .blwae(s.getBlwae())
                .fehler(s.getFehler())
                .kostl(s.getKostl())
                .mwskz(s.getMwskz())
                .aufnr(s.getAufnr())
                .build();
    }

    private static Satzart250 map(final Satzart250Type s) {
        return Satzart250.builder()
                .satzart(s.getSatzart())
                .psobkey(s.getPsobkey())
                .einnahmeart(s.getEinnahmeart())
                .betrw(s.getBetrw())
                .faedn(s.getFaedn())
                .bldat(s.getBldat())
                .persl(s.getPersl())
                .optxt(s.getOptxt())
                .sgtxt(s.getSgtxt())
                .blwae(s.getBlwae())
                .valut(s.getValut())
                .xblnr(s.getXblnr())
                .fvBelnr(s.getFvBelnr())
                .kostl(s.getKostl())
                .fehler(s.getFehler())
                .mwskz(s.getMwskz())
                .aufnr(s.getAufnr())
                .build();
    }

    private static Satzart260 map(final Satzart260Type s) {
        return Satzart260.builder()
                .satzart(s.getSatzart())
                .psobkey(s.getPsobkey())
                .einnahmeart(s.getEinnahmeart())
                .betrw(s.getBetrw())
                .faedn(s.getFaedn())
                .bldat(s.getBldat())
                .persl(s.getPersl())
                .xblnr(s.getXblnr())
                .fvBelnr(s.getFvBelnr())
                .fehler(s.getFehler())
                .kostl(s.getKostl())
                .mwskz(s.getMwskz())
                .aufnr(s.getAufnr())
                .build();
    }

    private static SatzartFehler map(final SatzartFehlerType s) {
        return SatzartFehler.builder()
                .satzart(s.getSatzart())
                .fehlertext(s.getFehlertext())
                .build();
    }
    // CPD-ON
}
