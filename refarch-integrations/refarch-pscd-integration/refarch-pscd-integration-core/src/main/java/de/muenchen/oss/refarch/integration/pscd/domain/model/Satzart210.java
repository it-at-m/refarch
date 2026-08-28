package de.muenchen.oss.refarch.integration.pscd.domain.model;

import lombok.Builder;
import lombok.Value;

/**
 * Record variant (SATZART {@code 210}).
 */
@Value
@Builder
public class Satzart210 {

    String satzart;
    String psobkey;
    String einnahmeart;
    String betrw;
    String faedn;
    String bldat;
    String persl;
    String xblnr;
    String fvBelnr;
    String blwae;
    String fehler;
    String kostl;
    String mwskz;
    String aufnr;
}
