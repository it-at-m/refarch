package de.muenchen.oss.refarch.integration.pscd.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class Satzart010Test {

    @Test
    void stripsTheLeadingZerosOffAbstimmsumme() {
        // The checksum as the fixed-width file carries it: sixteen columns, zero-padded on the left.
        assertThat(abstimmsumme("0000002648524762")).isEqualTo("2648524762");
        assertThat(abstimmsumme("0000000000070667")).isEqualTo("70667");
    }

    @Test
    void keepsAnAllZeroAbstimmsummeAsASingleZero() {
        assertThat(abstimmsumme("0000000000000000")).isEqualTo("0");
        assertThat(abstimmsumme("0")).isEqualTo("0");
    }

    @Test
    void leavesAnAbstimmsummeWithoutLeadingZerosUnchanged() {
        assertThat(abstimmsumme("12345")).isEqualTo("12345");
        // Only leading zeros go; the trailing ones are part of the number.
        assertThat(abstimmsumme("1000")).isEqualTo("1000");
    }

    /**
     * Blank stays blank and absent stays absent, so the mandatory-field check still sees a record that
     * is missing its checksum instead of one this builder quietly filled in.
     */
    @Test
    void leavesAnAbsentOrBlankAbstimmsummeAlone() {
        assertThat(abstimmsumme(null)).isNull();
        assertThat(abstimmsumme("")).isEmpty();
        assertThat(abstimmsumme("   ")).isEqualTo("   ");
    }

    @Test
    void leavesTheOtherFieldsUntouched() {
        final Satzart010 record = Satzart010.builder()
                .satzart("010").abstimmsumme("0001").vorzeichen("+").fehler("err")
                .build();

        assertThat(record.getSatzart()).isEqualTo("010");
        assertThat(record.getVorzeichen()).isEqualTo("+");
        assertThat(record.getFehler()).isEqualTo("err");
    }

    private static String abstimmsumme(final String value) {
        return Satzart010.builder().satzart("010").abstimmsumme(value).vorzeichen("+").build().getAbstimmsumme();
    }
}
