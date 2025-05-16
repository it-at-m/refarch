package de.muenchen.refarch.integration.dms.application.port.in;

import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Content;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public interface ReadContentInPort {

    List<Content> readContent(List<String> fileCoos, @NotBlank String user) throws DmsException;

}
