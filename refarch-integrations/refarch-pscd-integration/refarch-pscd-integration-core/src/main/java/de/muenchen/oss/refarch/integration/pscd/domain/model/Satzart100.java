package de.muenchen.oss.refarch.integration.pscd.domain.model;

import lombok.Builder;
import lombok.Value;

/**
 * Record (SATZART {@code 100}).
 */
@Value
@Builder
@SuppressWarnings("PMD.TooManyFields")
public class Satzart100 {

    String satzart;
    String psobkey;
    String partner;
    String addrnum;
    String psobtxtb1;
    String psobtxtb2;
    String psobtxtb3;
    String psobtxtb4;
    String psobtxtb5;
    String psobtxtb6;
    String zweitschuldner;
    String eigentuemerwechsel;
    String betriebsende;
    String kdKenn;
    String fachdstSb;
    String fachdstTelnr;
    String fehler;
}
