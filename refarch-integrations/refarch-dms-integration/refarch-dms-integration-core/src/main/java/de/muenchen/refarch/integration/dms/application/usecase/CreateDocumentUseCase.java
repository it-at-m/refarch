package de.muenchen.refarch.integration.dms.application.usecase;

import de.muenchen.refarch.integration.dms.application.port.out.CreateDocumentOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.ListContentOutPort;
import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Content;
import de.muenchen.refarch.integration.dms.domain.model.Document;
import de.muenchen.refarch.integration.dms.domain.model.DocumentResponse;
import de.muenchen.refarch.integration.dms.domain.model.DocumentType;
import de.muenchen.refarch.integration.dms.application.port.in.CreateDocumentInPort;
import de.muenchen.refarch.integration.dms.application.port.out.LoadFileOutPort;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;

@Validated
@RequiredArgsConstructor
public class CreateDocumentUseCase implements CreateDocumentInPort {

    private final CreateDocumentOutPort createDocumentOutPort;

    private final LoadFileOutPort loadFileOutPort;
    private final ListContentOutPort listContentOutPort;

    @Override
    public DocumentResponse createDocument(
            final String procedureCOO,
            final String title,
            final LocalDate date,
            final String user,
            final DocumentType type,
            final List<String> filepaths) throws DmsException, DocumentStorageException {

        final List<Content> contents = loadFileOutPort.loadFiles(filepaths);

        final Document document = new Document(procedureCOO, title, date, type, contents);

        String documentCoo = createDocumentOutPort.createDocument(document, user);
        List<String> contentCoos = listContentOutPort.listContentCoos(documentCoo, user);

        return new DocumentResponse(documentCoo, contentCoos);
    }
}
