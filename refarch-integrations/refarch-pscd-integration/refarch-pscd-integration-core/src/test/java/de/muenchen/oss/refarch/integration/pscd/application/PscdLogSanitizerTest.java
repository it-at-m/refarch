package de.muenchen.oss.refarch.integration.pscd.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PscdLogSanitizerTest {

    @Test
    void replacesAMissingFilenameWithAPlaceholder() {
        assertThat(PscdLogSanitizer.safeFilename(null)).isEqualTo(PscdLogSanitizer.NO_FILENAME);
        assertThat(PscdLogSanitizer.safeFilename("   ")).isEqualTo(PscdLogSanitizer.NO_FILENAME);
    }

    @Test
    void replacesControlCharactersSoAFilenameCannotForgeALogLine() {
        assertThat(PscdLogSanitizer.safeFilename("batch.dat\r\nINFO  forged line")).isEqualTo("batch.dat??INFO  forged line");
        assertThat(PscdLogSanitizer.safeFilename("batch.dat")).isEqualTo("batch.dat");
    }

    @Test
    void truncatesAnOversizedFilename() {
        final String sanitized = PscdLogSanitizer.safeFilename("x".repeat(300));

        assertThat(sanitized).hasSize(258).endsWith("...");
    }
}
