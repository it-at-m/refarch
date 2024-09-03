package de.muenchen.refarch.integration.dms.application.port.in;

import jakarta.validation.constraints.NotBlank;

public interface CreateFileInPort {

    String createFile(@NotBlank final String titel, @NotBlank final String apentryCOO, @NotBlank final String user);

}
