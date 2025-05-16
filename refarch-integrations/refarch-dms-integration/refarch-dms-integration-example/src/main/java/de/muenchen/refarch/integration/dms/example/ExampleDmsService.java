package de.muenchen.refarch.integration.dms.example;

import de.muenchen.refarch.integration.dms.application.port.in.CreateDocumentInPort;
import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Content;
import de.muenchen.refarch.integration.dms.domain.model.DocumentResponse;
import de.muenchen.refarch.integration.dms.domain.model.DocumentType;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExampleDmsService {
    private final DmsExampleProperties dmsExampleProperties;
    private final CreateDocumentInPort createDocumentInPort;

    void testCreateDocument() throws IOException, DmsException {
        try (InputStream file = new ClassPathResource("/files/test-pdf.pdf").getInputStream()) {
            final LocalDate date = LocalDate.now();
            final Content content = new Content("pdf", "Example ContentObject", file.readAllBytes());
            final DocumentResponse response = this.createDocumentInPort.createDocument(
                    dmsExampleProperties.getTargetProcedureCoo(),
                    "Refarch Example - Test Document",
                    date,
                    dmsExampleProperties.getUsername(),
                    DocumentType.EINGEHEND,
                    List.of(content));
            log.info("Successfully created document {}", response.documentCoo());
        }
    }
}
