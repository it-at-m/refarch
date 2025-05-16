package de.muenchen.refarch.integration.dms.application.usecase;

import de.muenchen.refarch.integration.dms.application.port.in.ReadContentInPort;
import de.muenchen.refarch.integration.dms.application.port.out.ReadContentOutPort;
import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Content;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
@RequiredArgsConstructor
public class ReadContentUseCase implements ReadContentInPort {

    private final ReadContentOutPort readContentOutPort;

    @Override
    public List<Content> readContent(
            final List<String> contentCoos,
            @NotBlank final String user) throws DmsException {
        return readContentOutPort.readContent(contentCoos, user);
    }
}
