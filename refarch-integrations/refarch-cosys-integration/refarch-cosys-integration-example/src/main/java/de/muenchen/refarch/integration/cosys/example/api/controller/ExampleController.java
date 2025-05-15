package de.muenchen.refarch.integration.cosys.example.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.muenchen.refarch.integration.cosys.application.port.out.GenerateDocumentOutPort;
import de.muenchen.refarch.integration.cosys.domain.exception.CosysException;
import de.muenchen.refarch.integration.cosys.domain.model.GenerateDocument;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ExampleController {

    private final GenerateDocumentOutPort generateDocumentOutPort;

    @PostMapping(value = "/test/document", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> testCreateCosysDocument() throws CosysException, IOException {
        try (InputStream pdfContent = this.generateDocumentOutPort.generateCosysDocument(this.generateDocument()).block()) {
            assert pdfContent != null;
            final InputStreamResource fileResource = new InputStreamResource(pdfContent);
            return ResponseEntity.ok(fileResource);
        }
    }

    private GenerateDocument generateDocument() {
        return GenerateDocument.builder()
                .client("9001")
                .role("TESTER")
                .guid("519650b7-87c2-41a6-8527-7b095675b13f")
                .variables(new ObjectMapper().valueToTree(Map.of(
                        "FormField_Grusstext", "Hallo das ist mein Gruß",
                        "EmpfaengerVorname", "Max",
                        "AbsenderVorname", "Mustermann")))
                .build();
    }

}
