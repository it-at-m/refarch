package de.muenchen.oss.refarch.integration.pscd.domain.model;

import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * Domain representation of a PSCD "Satzarten" batch. The aggregate that maps onto the SOAP
 * contract {@code DT_SOAPSatzarten}.
 *
 * <p>
 * A batch carries a {@code filename} (the source it was read from), a single
 * ({@link Satzart010} record and any number of the repeatable record types.
 * </p>
 */
@Value
@Builder
public class PscdSatzarten {

    String filename;

    Satzart010 satzart010;

    @Builder.Default
    List<Satzart100> satzart100 = List.of();

    @Builder.Default
    List<Satzart105> satzart105 = List.of();

    @Builder.Default
    List<Satzart155> satzart155 = List.of();

    @Builder.Default
    List<Satzart165> satzart165 = List.of();

    @Builder.Default
    List<Satzart200> satzart200 = List.of();

    @Builder.Default
    List<Satzart210> satzart210 = List.of();

    @Builder.Default
    List<Satzart250> satzart250 = List.of();

    @Builder.Default
    List<Satzart260> satzart260 = List.of();

    @Builder.Default
    List<SatzartFehler> satzartFehler = List.of();
}
