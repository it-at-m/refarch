package de.muenchen.oss.refarch.integration.pscd.application;

import java.util.regex.Pattern;

public final class PscdLogSanitizer {

    public static final String NO_FILENAME = "<none>";

    private static final int MAX_FILENAME_LENGTH = 255;

    private static final String TRUNCATION_MARKER = "...";

    private static final Pattern CONTROL_CHARACTERS = Pattern.compile("\\p{Cntrl}");

    private PscdLogSanitizer() {
    }

    /**
     * Make a caller-supplied batch filename safe to log. The REST and SOAP payloads and an embedding
     * application pass it on verbatim, so it is untrusted input. Control characters are replaced,
     * above all CR and LF, which would otherwise forge additional log lines. The value is truncated.
     *
     * @param filename the raw filename (may be {@code null})
     * @return a single-line, length-bounded replacement, never {@code null}
     */
    public static String safeFilename(final String filename) {
        if (filename == null || filename.isBlank()) {
            return NO_FILENAME;
        }
        final String sanitized = CONTROL_CHARACTERS.matcher(filename).replaceAll("?");
        if (sanitized.length() <= MAX_FILENAME_LENGTH) {
            return sanitized;
        }
        return sanitized.substring(0, MAX_FILENAME_LENGTH) + TRUNCATION_MARKER;
    }
}
