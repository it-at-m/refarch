package de.muenchen.oss.digiwf.dms.integration.application.port.in;

import jakarta.validation.constraints.NotBlank;

public interface DepositObjectInPort {

    void depositObject(@NotBlank final String objectCoo, @NotBlank final String user);

}
