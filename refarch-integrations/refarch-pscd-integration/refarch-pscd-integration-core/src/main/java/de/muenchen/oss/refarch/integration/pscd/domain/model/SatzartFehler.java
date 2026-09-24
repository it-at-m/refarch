package de.muenchen.oss.refarch.integration.pscd.domain.model;

import lombok.Builder;
import lombok.Value;

/**
 * Error record (SATZART {@code Fehler}) carrying a free-text error message for a record type.
 */
@Value
@Builder
public class SatzartFehler {

    String satzart;
    String fehlertext;
}
