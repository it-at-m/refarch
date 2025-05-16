package de.muenchen.refarch.integration.dms.application.usecase;

import de.muenchen.refarch.integration.dms.application.port.in.CreateDocumentInPort;
import de.muenchen.refarch.integration.dms.application.port.out.CreateDocumentOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.ListContentOutPort;
import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Content;
import de.muenchen.refarch.integration.dms.domain.model.Document;
import de.muenchen.refarch.integration.dms.domain.model.DocumentResponse;
import de.muenchen.refarch.integration.dms.domain.model.DocumentType;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
@RequiredArgsConstructor
public class CreateDocumentUseCase implements CreateDocumentInPort {

    private final CreateDocumentOutPort createDocumentOutPort;
    private final ListContentOutPort listContentOutPort;

    @Override
    public DocumentResponse createDocument(
            final String procedureCOO,
            final String title,
            final LocalDate date,
            final String user,
            final DocumentType type,
            final List<Content> contents) throws DmsException {
        final Document document = new Document(procedureCOO, title, date, type, contents);

        final String documentCoo = createDocumentOutPort.createDocument(document, user);
        final List<String> contentCoos = listContentOutPort.listContentCoos(documentCoo, user);

        return new DocumentResponse(documentCoo, contentCoos);
    }
}
