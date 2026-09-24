package de.muenchen.oss.refarch.integration.pscd.adapter.out.pscd;

import de.lhm.pi.pscd.rahmenschnittstelle.DTSOAPSatzarten;
import de.lhm.pi.pscd.rahmenschnittstelle.DTSatzart010;
import de.lhm.pi.pscd.rahmenschnittstelle.DTSatzart100;
import de.lhm.pi.pscd.rahmenschnittstelle.DTSatzart105;
import de.lhm.pi.pscd.rahmenschnittstelle.DTSatzart155;
import de.lhm.pi.pscd.rahmenschnittstelle.DTSatzart165;
import de.lhm.pi.pscd.rahmenschnittstelle.DTSatzart200;
import de.lhm.pi.pscd.rahmenschnittstelle.DTSatzart210;
import de.lhm.pi.pscd.rahmenschnittstelle.DTSatzart250;
import de.lhm.pi.pscd.rahmenschnittstelle.DTSatzart260;
import de.lhm.pi.pscd.rahmenschnittstelle.DTSatzartFehler;
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

/**
 * Maps the {@link PscdSatzarten} domain aggregate onto the generated SOAP contract
 * ({@link DTSOAPSatzarten}).
 */
// Hand-written because the generated accessors are upper-case while the domain uses camel-case
@SuppressWarnings("PMD.CouplingBetweenObjects")
final class PscdSatzartenMapper {

    private PscdSatzartenMapper() {
    }

    // CPD-OFF - SATZART types share field blocks, so the per-record mappers are near-identical by design
    /* default */ static DTSOAPSatzarten toDto(final PscdSatzarten source) {
        final DTSOAPSatzarten dto = new DTSOAPSatzarten();
        dto.setFilename(source.getFilename());
        if (source.getSatzart010() != null) {
            dto.setSatzart010(map(source.getSatzart010()));
        }
        source.getSatzart100().forEach(record -> dto.getSatzart100().add(map(record)));
        source.getSatzart105().forEach(record -> dto.getSatzart105().add(map(record)));
        source.getSatzart155().forEach(record -> dto.getSatzart155().add(map(record)));
        source.getSatzart165().forEach(record -> dto.getSatzart165().add(map(record)));
        source.getSatzart200().forEach(record -> dto.getSatzart200().add(map(record)));
        source.getSatzart210().forEach(record -> dto.getSatzart210().add(map(record)));
        source.getSatzart250().forEach(record -> dto.getSatzart250().add(map(record)));
        source.getSatzart260().forEach(record -> dto.getSatzart260().add(map(record)));
        source.getSatzartFehler().forEach(record -> dto.getSatzartFehler().add(map(record)));
        return dto;
    }

    private static DTSatzart010 map(final Satzart010 s) {
        final DTSatzart010 t = new DTSatzart010();
        t.setSATZART(s.getSatzart());
        t.setABSTIMMSUMME(s.getAbstimmsumme());
        t.setVORZEICHEN(s.getVorzeichen());
        t.setFEHLER(s.getFehler());
        return t;
    }

    private static DTSatzart100 map(final Satzart100 s) {
        final DTSatzart100 t = new DTSatzart100();
        t.setSATZART(s.getSatzart());
        t.setPSOBKEY(s.getPsobkey());
        t.setPARTNER(s.getPartner());
        t.setADDRNUM(s.getAddrnum());
        t.setPSOBTXTB1(s.getPsobtxtb1());
        t.setPSOBTXTB2(s.getPsobtxtb2());
        t.setPSOBTXTB3(s.getPsobtxtb3());
        t.setPSOBTXTB4(s.getPsobtxtb4());
        t.setPSOBTXTB5(s.getPsobtxtb5());
        t.setPSOBTXTB6(s.getPsobtxtb6());
        t.setZWEITSCHULDNER(s.getZweitschuldner());
        t.setEIGENTUEMERWECHSEL(s.getEigentuemerwechsel());
        t.setBETRIEBSENDE(s.getBetriebsende());
        t.setKDKENN(s.getKdKenn());
        t.setFACHDSTSB(s.getFachdstSb());
        t.setFACHDSTTELNR(s.getFachdstTelnr());
        t.setFEHLER(s.getFehler());
        return t;
    }

    private static DTSatzart105 map(final Satzart105 s) {
        final DTSatzart105 t = new DTSatzart105();
        t.setSATZART(s.getSatzart());
        t.setPSOBKEY(s.getPsobkey());
        t.setCORRPARTNER(s.getCorrPartner());
        t.setCORRROLE(s.getCorrRole());
        t.setFEHLER(s.getFehler());
        return t;
    }

    private static DTSatzart155 map(final Satzart155 s) {
        final DTSatzart155 t = new DTSatzart155();
        t.setSATZART(s.getSatzart());
        t.setPSOBKEY(s.getPsobkey());
        t.setDELETEFLAG(s.getDeleteFlag());
        t.setCORRROLE(s.getCorrRole());
        t.setFEHLER(s.getFehler());
        return t;
    }

    private static DTSatzart165 map(final Satzart165 s) {
        final DTSatzart165 t = new DTSatzart165();
        t.setSATZART(s.getSatzart());
        t.setPSOBKEY(s.getPsobkey());
        t.setPSOBTXTB1(s.getPsobtxtb1());
        t.setPSOBTXTB2(s.getPsobtxtb2());
        t.setPSOBTXTB3(s.getPsobtxtb3());
        t.setPSOBTXTB4(s.getPsobtxtb4());
        t.setPSOBTXTB5(s.getPsobtxtb5());
        t.setPSOBTXTB6(s.getPsobtxtb6());
        t.setZWEITSCHULDNER(s.getZweitschuldner());
        t.setEIGENTUEMERWECHSEL(s.getEigentuemerwechsel());
        t.setBETRIEBSENDE(s.getBetriebsende());
        t.setKDKENN(s.getKdKenn());
        t.setFACHDSTSB(s.getFachdstSb());
        t.setFACHDSTTELNR(s.getFachdstTelnr());
        t.setFEHLER(s.getFehler());
        return t;
    }

    private static DTSatzart200 map(final Satzart200 s) {
        final DTSatzart200 t = new DTSatzart200();
        t.setSATZART(s.getSatzart());
        t.setPSOBKEY(s.getPsobkey());
        t.setEINNAHMEART(s.getEinnahmeart());
        t.setBETRW(s.getBetrw());
        t.setFAEDN(s.getFaedn());
        t.setBLDAT(s.getBldat());
        t.setPERSL(s.getPersl());
        t.setOPTXT(s.getOptxt());
        t.setSGTXT(s.getSgtxt());
        t.setBLWAE(s.getBlwae());
        t.setXBLNR(s.getXblnr());
        t.setFVBELNR(s.getFvBelnr());
        t.setKOSTL(s.getKostl());
        t.setFEHLER(s.getFehler());
        t.setMWSKZ(s.getMwskz());
        t.setAUFNR(s.getAufnr());
        return t;
    }

    private static DTSatzart210 map(final Satzart210 s) {
        final DTSatzart210 t = new DTSatzart210();
        t.setSATZART(s.getSatzart());
        t.setPSOBKEY(s.getPsobkey());
        t.setEINNAHMEART(s.getEinnahmeart());
        t.setBETRW(s.getBetrw());
        t.setFAEDN(s.getFaedn());
        t.setBLDAT(s.getBldat());
        t.setPERSL(s.getPersl());
        t.setXBLNR(s.getXblnr());
        t.setFVBELNR(s.getFvBelnr());
        t.setBLWAE(s.getBlwae());
        t.setFEHLER(s.getFehler());
        t.setKOSTL(s.getKostl());
        t.setMWSKZ(s.getMwskz());
        t.setAUFNR(s.getAufnr());
        return t;
    }

    private static DTSatzart250 map(final Satzart250 s) {
        final DTSatzart250 t = new DTSatzart250();
        t.setSATZART(s.getSatzart());
        t.setPSOBKEY(s.getPsobkey());
        t.setEINNAHMEART(s.getEinnahmeart());
        t.setBETRW(s.getBetrw());
        t.setFAEDN(s.getFaedn());
        t.setBLDAT(s.getBldat());
        t.setPERSL(s.getPersl());
        t.setOPTXT(s.getOptxt());
        t.setSGTXT(s.getSgtxt());
        t.setBLWAE(s.getBlwae());
        t.setVALUT(s.getValut());
        t.setXBLNR(s.getXblnr());
        t.setFVBELNR(s.getFvBelnr());
        t.setKOSTL(s.getKostl());
        t.setFEHLER(s.getFehler());
        t.setMWSKZ(s.getMwskz());
        t.setAUFNR(s.getAufnr());
        return t;
    }

    private static DTSatzart260 map(final Satzart260 s) {
        final DTSatzart260 t = new DTSatzart260();
        t.setSATZART(s.getSatzart());
        t.setPSOBKEY(s.getPsobkey());
        t.setEINNAHMEART(s.getEinnahmeart());
        t.setBETRW(s.getBetrw());
        t.setFAEDN(s.getFaedn());
        t.setBLDAT(s.getBldat());
        t.setPERSL(s.getPersl());
        t.setXBLNR(s.getXblnr());
        t.setFVBELNR(s.getFvBelnr());
        t.setFEHLER(s.getFehler());
        t.setKOSTL(s.getKostl());
        t.setMWSKZ(s.getMwskz());
        t.setAUFNR(s.getAufnr());
        return t;
    }

    private static DTSatzartFehler map(final SatzartFehler s) {
        final DTSatzartFehler t = new DTSatzartFehler();
        t.setSATZART(s.getSatzart());
        t.setFEHLERTEXT(s.getFehlertext());
        return t;
    }
    // CPD-ON
}
