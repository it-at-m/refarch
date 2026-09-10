package de.muenchen.oss.refarch.integration.pscd.domain.model;

import lombok.Builder;
import lombok.Value;

/**
 * Record with value date (SATZART {@code 250}).
 */
@Value
@Builder
@SuppressWarnings("PMD.TooManyFields")
public class Satzart250 {

    String satzart;
    String psobkey;
    String einnahmeart;
    String betrw;
    String faedn;
    String bldat;
    String persl;
    String optxt;
    String sgtxt;
    String blwae;
    String valut;
    String xblnr;
    String fvBelnr;
    String kostl;
    String fehler;
    String mwskz;
    String aufnr;
}
