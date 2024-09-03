package de.muenchen.refarch.integration.dms.application.port.in;

import jakarta.validation.constraints.NotBlank;

public interface CancelObjectInPort {

    void cancelObject(@NotBlank final String objectCoo, @NotBlank final String user);

}
