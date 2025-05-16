package de.muenchen.refarch.integration.dms.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.refarch.integration.dms.application.port.out.CreateDocumentOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.ListContentOutPort;
import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Content;
import de.muenchen.refarch.integration.dms.domain.model.Document;
import de.muenchen.refarch.integration.dms.domain.model.DocumentResponse;
import de.muenchen.refarch.integration.dms.domain.model.DocumentType;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class CreateDocumentUseCaseTest {

    private final CreateDocumentOutPort createDocumentOutPort = mock(CreateDocumentOutPort.class);

    private final ListContentOutPort listContentOutPort = mock(ListContentOutPort.class);

    private final CreateDocumentUseCase createDocumentUseCase = new CreateDocumentUseCase(createDocumentOutPort, listContentOutPort);

    @Test
    void createDocument() throws DmsException {
        final Content content = new Content("extension", "name", "content".getBytes());
        final LocalDate testDate = LocalDate.parse("2023-12-01");
        final String docCoo = "documentCOO";
        final String user = "user";
        final List<String> fileCoos = List.of("contentCoo1", "contentCoo2");

        when(this.createDocumentOutPort.createDocument(any(), any())).thenReturn(docCoo);
        when(this.listContentOutPort.listContentCoos(docCoo, user)).thenReturn(fileCoos);

        final DocumentResponse documentResponse = createDocumentUseCase.createDocument("procedureCOO", "title", testDate, "user", DocumentType.EINGEHEND,
                List.of(content));

        assertEquals(docCoo, documentResponse.documentCoo());
        assertEquals(fileCoos, documentResponse.contentCoos());
        verify(this.createDocumentOutPort, times(1)).createDocument(new Document("procedureCOO", "title", testDate, DocumentType.EINGEHEND, List.of(content)),
                user);
        verify(this.listContentOutPort, times(1)).listContentCoos(docCoo, user);
    }
}
