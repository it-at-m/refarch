package de.muenchen.oss.refarch.integration.pscd.domain.model;

import lombok.Builder;
import lombok.Value;

/**
 * Record (SATZART {@code 155}).
 */
@Value
@Builder
public class Satzart155 {

    String satzart;
    String psobkey;
    String deleteFlag;
    String corrRole;
    String fehler;
}
