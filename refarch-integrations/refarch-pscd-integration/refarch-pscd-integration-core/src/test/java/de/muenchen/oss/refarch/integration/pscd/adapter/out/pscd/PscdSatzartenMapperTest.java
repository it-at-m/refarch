package de.muenchen.oss.refarch.integration.pscd.adapter.out.pscd;

import static org.assertj.core.api.Assertions.assertThat;

import de.lhm.pi.pscd.rahmenschnittstelle.DTSOAPSatzarten;
import de.lhm.pi.pscd.rahmenschnittstelle.DTSatzart200;
import de.muenchen.oss.refarch.integration.pscd.domain.model.PscdSatzarten;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart010;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart200;
import java.util.List;
import org.junit.jupiter.api.Test;

class PscdSatzartenMapperTest {

    @Test
    void mapsAllFieldsOfARichRecord() {
        final Satzart200 source = Satzart200.builder()
                .satzart("200").psobkey("KEY").einnahmeart("EA").betrw("100,00")
                .faedn("20260101").bldat("20260102").persl("0001").optxt("open text")
                .sgtxt("seg text").blwae("EUR").xblnr("INV-1").fvBelnr("FV-1")
                .kostl("CC-1").fehler("err").mwskz("V1").aufnr("ORDER-1")
                .build();

        final DTSOAPSatzarten dto = PscdSatzartenMapper.toDto(
                PscdSatzarten.builder().filename("f").satzart200(List.of(source)).build());

        final DTSatzart200 target = dto.getSatzart200().get(0);
        assertThat(target.getSATZART()).isEqualTo("200");
        assertThat(target.getPSOBKEY()).isEqualTo("KEY");
        assertThat(target.getEINNAHMEART()).isEqualTo("EA");
        assertThat(target.getBETRW()).isEqualTo("100,00");
        assertThat(target.getFAEDN()).isEqualTo("20260101");
        assertThat(target.getBLDAT()).isEqualTo("20260102");
        assertThat(target.getPERSL()).isEqualTo("0001");
        assertThat(target.getOPTXT()).isEqualTo("open text");
        assertThat(target.getSGTXT()).isEqualTo("seg text");
        assertThat(target.getBLWAE()).isEqualTo("EUR");
        assertThat(target.getXBLNR()).isEqualTo("INV-1");
        assertThat(target.getFVBELNR()).isEqualTo("FV-1");
        assertThat(target.getKOSTL()).isEqualTo("CC-1");
        assertThat(target.getFEHLER()).isEqualTo("err");
        assertThat(target.getMWSKZ()).isEqualTo("V1");
        assertThat(target.getAUFNR()).isEqualTo("ORDER-1");
    }

    /**
     * The unpadding itself is {@code Satzart010}'s, and tested there; this only pins that the mapper
     * passes the normalised value on rather than re-deriving one.
     */
    @Test
    void sendsTheControlRecordsCheckSumAsTheDomainCarriesIt() {
        final DTSOAPSatzarten dto = PscdSatzartenMapper.toDto(PscdSatzarten.builder()
                .filename("f")
                .satzart010(Satzart010.builder().satzart("010").abstimmsumme("0000002648524762").vorzeichen("+").build())
                .build());

        assertThat(dto.getSatzart010().getABSTIMMSUMME()).isEqualTo("2648524762");
        assertThat(dto.getSatzart010().getVORZEICHEN()).isEqualTo("+");
    }

    @Test
    void mapsEmptyBatchWithoutError() {
        final DTSOAPSatzarten dto = PscdSatzartenMapper.toDto(PscdSatzarten.builder().filename("f").build());

        assertThat(dto.getFilename()).isEqualTo("f");
        assertThat(dto.getSatzart010()).isNull();
        assertThat(dto.getSatzart200()).isEmpty();
    }
}
