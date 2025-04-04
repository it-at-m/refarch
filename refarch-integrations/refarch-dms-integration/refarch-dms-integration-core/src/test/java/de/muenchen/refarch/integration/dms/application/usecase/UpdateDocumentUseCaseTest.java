package de.muenchen.refarch.integration.dms.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.refarch.integration.dms.application.port.out.ListContentOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.LoadFileOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.UpdateDocumentOutPort;
import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Content;
import de.muenchen.refarch.integration.dms.domain.model.DocumentResponse;
import de.muenchen.refarch.integration.dms.domain.model.DocumentType;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import java.util.List;
import org.junit.jupiter.api.Test;

class UpdateDocumentUseCaseTest {

    private final LoadFileOutPort loadFileOutPort = mock(LoadFileOutPort.class);
    private final ListContentOutPort listContentOutPort = mock(ListContentOutPort.class);

    private final UpdateDocumentOutPort updateDocumentOutPort = mock(UpdateDocumentOutPort.class);

    private final UpdateDocumentUseCase updateDocumentUseCase = new UpdateDocumentUseCase(updateDocumentOutPort, listContentOutPort, loadFileOutPort);

    @Test
    void updateDocument() throws DmsException, DocumentStorageException {

        final Content content = new Content("extension", "name", "content".getBytes());

        final List<String> filepaths = List.of("path/content.pdf");
        final String docCoo = "documentCOO";
        final String user = "user";
        final List<String> fileCoos = List.of("contentCoo1", "contentCoo2");

        when(this.loadFileOutPort.loadFiles(any())).thenReturn(List.of(content));
        when(this.listContentOutPort.listContentCoos(docCoo, user)).thenReturn(fileCoos);

        doNothing().when(updateDocumentOutPort).updateDocument(any(), any(), any(), any());

        final DocumentResponse documentResponse = updateDocumentUseCase.updateDocument(docCoo, "user", DocumentType.EINGEHEND, filepaths);

        assertEquals(docCoo, documentResponse.documentCoo());
        assertEquals(fileCoos, documentResponse.contentCoos());
        verify(this.loadFileOutPort, times(1)).loadFiles(filepaths);
        verify(this.updateDocumentOutPort, times(1)).updateDocument(docCoo, DocumentType.EINGEHEND, List.of(content), "user");

    }
}
