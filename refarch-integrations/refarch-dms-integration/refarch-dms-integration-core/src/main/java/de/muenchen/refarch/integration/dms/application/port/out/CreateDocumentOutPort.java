package de.muenchen.refarch.integration.dms.application.port.out;

import de.muenchen.refarch.integration.dms.domain.model.Document;

public interface CreateDocumentOutPort {

    String createDocument(Document document, String user);

}
