package de.muenchen.oss.refarch.integration.pscd.service.adapter.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.muenchen.oss.refarch.integration.pscd.domain.exception.PscdValidationException;
import de.muenchen.oss.refarch.integration.pscd.domain.model.PscdSatzarten;
import de.muenchen.oss.refarch.integration.pscd.inbound.model.PscdBatch;
import de.muenchen.oss.refarch.integration.pscd.inbound.model.Satzart010Type;
import de.muenchen.oss.refarch.integration.pscd.inbound.model.Satzart200Type;
import org.junit.jupiter.api.Test;

class PscdInboundCanonicalMapperTest {

    @Test
    void mapsTheCanonicalBatchOntoTheDomainAggregate() {
        final PscdBatch source = new PscdBatch();
        source.setFilename("batch.json");
        final Satzart010Type control = new Satzart010Type();
        control.setSatzart("010");
        control.setAbstimmsumme("12345");
        control.setVorzeichen("+");
        source.setSatzart010(control);
        final Satzart200Type posting = new Satzart200Type();
        posting.setSatzart("200");
        posting.setPsobkey("KEY2");
        posting.setEinnahmeart("EA");
        posting.setBetrw("100,00");
        posting.setFaedn("20260131");
        posting.setBldat("20260101");
        posting.setXblnr("INV-1");
        posting.setFvBelnr("FV-1");
        source.getSatzart200().add(posting);

        final PscdSatzarten result = PscdInboundCanonicalMapper.toDomain(source);

        assertThat(result.getFilename()).isEqualTo("batch.json");
        assertThat(result.getSatzart010().getAbstimmsumme()).isEqualTo("12345");
        assertThat(result.getSatzart010().getVorzeichen()).isEqualTo("+");
        assertThat(result.getSatzart200()).singleElement()
                .satisfies(record -> {
                    assertThat(record.getPsobkey()).isEqualTo("KEY2");
                    assertThat(record.getBetrw()).isEqualTo("100,00");
                    assertThat(record.getFvBelnr()).isEqualTo("FV-1");
                });
    }

    @Test
    void rejectsANullPayload() {
        assertThatThrownBy(() -> PscdInboundCanonicalMapper.toDomain(null))
                .isInstanceOf(PscdValidationException.class);
    }

    @Test
    void validationExceptionIsAProcessingException() {
        // subtype relationship keeps existing PscdProcessingException handlers working
        assertThat(new PscdValidationException("x"))
                .isInstanceOf(de.muenchen.oss.refarch.integration.pscd.domain.exception.PscdProcessingException.class);
    }

    /**
     * The filename identifies the batch end to end (log line, delivery, reconciliation), so a batch
     * without one, absent or blank, is rejected. Blank matters: the canonical schema's
     * {@code minOccurs="1"} could never catch {@code <filename> </filename>}.
     */
    @Test
    void rejectsABatchMissingTheMandatoryFilename() {
        final PscdBatch source = new PscdBatch();
        source.setSatzart010(new Satzart010Type());

        assertThatThrownBy(() -> PscdInboundCanonicalMapper.toDomain(source))
                .isInstanceOf(PscdValidationException.class)
                .hasMessageContaining("filename");

        source.setFilename("   ");

        assertThatThrownBy(() -> PscdInboundCanonicalMapper.toDomain(source))
                .isInstanceOf(PscdValidationException.class)
                .hasMessageContaining("filename");
    }

    @Test
    void rejectsABatchMissingTheMandatoryControlRecord() {
        final PscdBatch source = new PscdBatch();
        source.setFilename("bad.json");

        assertThatThrownBy(() -> PscdInboundCanonicalMapper.toDomain(source))
                .isInstanceOf(PscdValidationException.class)
                .hasMessageContaining("Satzart010");
    }
}
