package de.muenchen.oss.refarch.integration.dms.application.port.out;

import de.muenchen.oss.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.oss.refarch.integration.dms.domain.model.RequestContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.validation.annotation.Validated;

@Validated
public interface SearchFileOutPort {

    List<String> searchFile(@NotBlank String searchString, @NotNull @Valid RequestContext requestContext, String reference, String value) throws DmsException;
}
