package de.muenchen.refarch.integration.dms.application.usecase;

import de.muenchen.refarch.integration.dms.domain.model.File;
import de.muenchen.refarch.integration.dms.application.port.in.CreateFileInPort;
import de.muenchen.refarch.integration.dms.application.port.out.CreateFileOutPort;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
@RequiredArgsConstructor
public class CreateFileUseCase implements CreateFileInPort {

    private final CreateFileOutPort createFileOutPort;

    @Override
    public String createFile(
            @NotBlank final String titel,
            @NotBlank final String apentryCOO,
            @NotBlank final String user
    ) {
        final File file = new File(apentryCOO, titel);

        return createFileOutPort.createFile(file, user);
    }

}
