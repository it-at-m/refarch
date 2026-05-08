package de.muenchen.oss.refarch.integration.dms.application.port.out;

import de.muenchen.oss.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.oss.refarch.integration.dms.domain.model.RequestContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.validation.annotation.Validated;

@Validated
public interface ListContentOutPort {
    /**
     * List all content coos for a document coo.
     *
     * @param documentCoo The document coo to list the content for.
     * @param requestContext The context to make the request under.
     * @return The list of content coos contained in the document.
     */
    List<String> listContentCoos(@NotNull String documentCoo, @NotNull @Valid RequestContext requestContext) throws DmsException;
}
