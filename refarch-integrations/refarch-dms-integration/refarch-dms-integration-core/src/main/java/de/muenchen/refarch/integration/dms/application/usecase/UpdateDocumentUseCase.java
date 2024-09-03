package de.muenchen.refarch.integration.dms.application.usecase;

import de.muenchen.refarch.integration.dms.application.port.out.ListContentOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.UpdateDocumentOutPort;
import de.muenchen.refarch.integration.dms.domain.model.Content;
import de.muenchen.refarch.integration.dms.domain.model.DocumentResponse;
import de.muenchen.refarch.integration.dms.domain.model.DocumentType;
import de.muenchen.refarch.integration.dms.application.port.in.UpdateDocumentInPort;
import de.muenchen.refarch.integration.dms.application.port.out.LoadFileOutPort;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@RequiredArgsConstructor
public class UpdateDocumentUseCase implements UpdateDocumentInPort {

    private final UpdateDocumentOutPort updateDocumentOutPort;
    private final ListContentOutPort listContentOutPort;

    private final LoadFileOutPort loadFileOutPort;

    @Override
    public DocumentResponse updateDocument(
            final String documentCOO,
            final String user,
            final DocumentType type,
            final List<String> filepaths,
            final String fileContext,
            final String processDefinition
    ) {

        final List<Content> contents = loadFileOutPort.loadFiles(filepaths, fileContext, processDefinition);

        updateDocumentOutPort.updateDocument(documentCOO, type, contents, user);
        List<String> contentCoos = listContentOutPort.listContentCoos(documentCOO, user);
        return new DocumentResponse(documentCOO, contentCoos);
    }


}
