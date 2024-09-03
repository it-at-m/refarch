package de.muenchen.refarch.integration.dms.application.port.out;

import de.muenchen.refarch.integration.dms.domain.Document;

public interface CreateDocumentOutPort {

    String createDocument(Document document, String user);

}
