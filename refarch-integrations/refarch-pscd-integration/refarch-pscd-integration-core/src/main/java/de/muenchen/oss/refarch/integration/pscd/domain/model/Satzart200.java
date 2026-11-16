package de.muenchen.oss.refarch.integration.pscd.domain.model;

import lombok.Builder;
import lombok.Value;

/**
 * Record (SATZART {@code 200}).
 */
@Value
@Builder
@SuppressWarnings("PMD.TooManyFields")
public class Satzart200 {

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
    String xblnr;
    String fvBelnr;
    String kostl;
    String fehler;
    String mwskz;
    String aufnr;
}
