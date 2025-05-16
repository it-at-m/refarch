package de.muenchen.refarch.integration.dms.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.refarch.integration.dms.application.port.out.ListContentOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.UpdateDocumentOutPort;
import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Content;
import de.muenchen.refarch.integration.dms.domain.model.DocumentResponse;
import de.muenchen.refarch.integration.dms.domain.model.DocumentType;
import java.util.List;
import org.junit.jupiter.api.Test;

class UpdateDocumentUseCaseTest {

    private final ListContentOutPort listContentOutPort = mock(ListContentOutPort.class);

    private final UpdateDocumentOutPort updateDocumentOutPort = mock(UpdateDocumentOutPort.class);

    private final UpdateDocumentUseCase updateDocumentUseCase = new UpdateDocumentUseCase(updateDocumentOutPort, listContentOutPort);

    @Test
    void updateDocument() throws DmsException {

        final Content content = new Content("extension", "name", "content".getBytes());

        final String docCoo = "documentCOO";
        final String user = "user";
        final List<String> fileCoos = List.of("contentCoo1", "contentCoo2");

        when(this.listContentOutPort.listContentCoos(docCoo, user)).thenReturn(fileCoos);

        doNothing().when(updateDocumentOutPort).updateDocument(any(), any(), any(), any());

        final DocumentResponse documentResponse = updateDocumentUseCase.updateDocument(docCoo, "user", DocumentType.EINGEHEND, List.of(content));

        assertEquals(docCoo, documentResponse.documentCoo());
        assertEquals(fileCoos, documentResponse.contentCoos());
        verify(this.updateDocumentOutPort, times(1)).updateDocument(docCoo, DocumentType.EINGEHEND, List.of(content), "user");

    }
}
