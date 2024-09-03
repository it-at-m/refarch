package de.muenchen.refarch.integration.dms.application.port.out;

import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Document;

public interface CreateDocumentOutPort {

    String createDocument(Document document, String user) throws DmsException;

}
