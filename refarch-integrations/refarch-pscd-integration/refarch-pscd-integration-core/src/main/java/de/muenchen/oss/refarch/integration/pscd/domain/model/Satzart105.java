package de.muenchen.oss.refarch.integration.pscd.domain.model;

import lombok.Builder;
import lombok.Value;

/**
 * Record (SATZART {@code 105}).
 */
@Value
@Builder
public class Satzart105 {

    String satzart;
    String psobkey;
    String corrPartner;
    String corrRole;
    String fehler;
}
