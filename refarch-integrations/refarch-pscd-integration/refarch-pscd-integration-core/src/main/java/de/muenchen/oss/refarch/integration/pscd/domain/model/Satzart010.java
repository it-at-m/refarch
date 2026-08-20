package de.muenchen.oss.refarch.integration.pscd.domain.model;

import lombok.Builder;
import lombok.Value;

/**
 * Record (SATZART {@code 010}).
 *
 * <p>
 * ABSTIMMSUMME is normalised while the record is built, not on the way out: the interchange formats
 * carry the value zero-padded to its column width while PSCD expects the plain number.
 * </p>
 */
@Value
@Builder
public class Satzart010 {

    String satzart;
    String abstimmsumme;
    String vorzeichen;
    String fehler;

    /** Hand-written for the one field that is normalised on the way in. */
    public static class Satzart010Builder {

        /**
         * Take the batch checksum without the leading zeros the interchange formats pad it with:
         * {@code 0000002648524762} becomes {@code 2648524762}. A checksum that is genuinely zero keeps
         * a single digit ({@code 0000000000000000} becomes {@code 0}) rather than collapsing to
         * nothing, which the mandatory-field check would then reject.
         */
        public Satzart010Builder abstimmsumme(final String abstimmsumme) {
            this.abstimmsumme = withoutLeadingZeros(abstimmsumme);
            return this;
        }

        private static String withoutLeadingZeros(final String value) {
            if (value == null) {
                return null;
            }
            int firstKept = 0;
            // Stops one short of the end, so an all-zero checksum keeps its last digit.
            while (firstKept < value.length() - 1 && value.charAt(firstKept) == '0') {
                firstKept++;
            }
            return value.substring(firstKept);
        }
    }
}
