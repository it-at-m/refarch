package de.muenchen.refarch.integration.dms.application.port.out;

import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import org.springframework.lang.NonNull;

import java.util.List;

public interface ListContentOutPort {
    /**
     * List all content coos for a document coo.
     *
     * @param documentCoo The document coo to list the content for.
     * @return The list of content coos contained in the document.
     */
    List<String> listContentCoos(@NonNull String documentCoo, @NonNull String user) throws DmsException;
}
