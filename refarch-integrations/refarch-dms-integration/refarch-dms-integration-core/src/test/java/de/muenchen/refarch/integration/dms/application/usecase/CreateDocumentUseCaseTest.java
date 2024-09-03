package de.muenchen.refarch.integration.dms.application.usecase;

import de.muenchen.refarch.integration.dms.application.port.out.CreateDocumentOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.ListContentOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.LoadFileOutPort;
import de.muenchen.refarch.integration.dms.domain.model.Content;
import de.muenchen.refarch.integration.dms.domain.model.Document;
import de.muenchen.refarch.integration.dms.domain.model.DocumentResponse;
import de.muenchen.refarch.integration.dms.domain.model.DocumentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateDocumentUseCaseTest {

    private final LoadFileOutPort loadFileOutPort = mock(LoadFileOutPort.class);

    private final CreateDocumentOutPort createDocumentOutPort = mock(CreateDocumentOutPort.class);

    private final ListContentOutPort listContentOutPort = mock(ListContentOutPort.class);

    private final CreateDocumentUseCase createDocumentUseCase = new CreateDocumentUseCase(createDocumentOutPort, loadFileOutPort, listContentOutPort);

    @Test
    void createDocument() {
        Content content = new Content("extension", "name", "content".getBytes());
        List<String> filepaths = List.of("path/content.pdf");
        LocalDate testDate = LocalDate.parse("2023-12-01");
        String docCoo = "documentCOO";
        String user = "user";
        List<String> fileCoos = List.of("contentCoo1", "contentCoo2");

        when(this.loadFileOutPort.loadFiles(any(), any(), any())).thenReturn(List.of(content));
        when(this.createDocumentOutPort.createDocument(any(), any())).thenReturn(docCoo);
        when(this.listContentOutPort.listContentCoos(docCoo, user)).thenReturn(fileCoos);

        DocumentResponse documentResponse = createDocumentUseCase.createDocument("procedureCOO", "title", testDate, "user", DocumentType.EINGEHEND, filepaths, "filecontext",
                "processDefinitionId");

        assertEquals(docCoo, documentResponse.getDocumentCoo());
        assertEquals(fileCoos, documentResponse.getContentCoos());
        verify(this.loadFileOutPort, times(1)).loadFiles(filepaths, "filecontext", "processDefinitionId");
        verify(this.createDocumentOutPort, times(1)).createDocument(new Document("procedureCOO", "title", testDate, DocumentType.EINGEHEND, List.of(content)),
                user);
        verify(this.listContentOutPort, times(1)).listContentCoos(docCoo, user);
    }
}
