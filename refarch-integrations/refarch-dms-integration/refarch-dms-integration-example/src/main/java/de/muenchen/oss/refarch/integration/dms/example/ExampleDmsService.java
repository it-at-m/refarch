package de.muenchen.oss.refarch.integration.dms.example;

import de.muenchen.oss.refarch.integration.dms.application.port.out.CreateDocumentOutPort;
import de.muenchen.oss.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.oss.refarch.integration.dms.domain.model.Content;
import de.muenchen.oss.refarch.integration.dms.domain.model.Document;
import de.muenchen.oss.refarch.integration.dms.domain.model.DocumentType;
import de.muenchen.oss.refarch.integration.dms.domain.model.RequestContext;
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
    private final CreateDocumentOutPort createDocumentOutPort;

    public void testCreateDocument() throws IOException, DmsException {
        try (InputStream file = new ClassPathResource("/files/test-pdf.pdf").getInputStream()) {
            final LocalDate date = LocalDate.now();
            final Content content = new Content("pdf", "Example ContentObject", file.readAllBytes());
            final Document document = new Document(
                    dmsExampleProperties.getTargetProcedureCoo(),
                    "Refarch Example - Test Document",
                    date,
                    DocumentType.EINGEHEND,
                    List.of(content));
            final RequestContext requestContext = new RequestContext(dmsExampleProperties.getUsername(), null, null);

            final String documentCoo = this.createDocumentOutPort.createDocument(
                    document,
                    requestContext);
            log.info("Successfully created document {}", documentCoo);
        }
    }
}
