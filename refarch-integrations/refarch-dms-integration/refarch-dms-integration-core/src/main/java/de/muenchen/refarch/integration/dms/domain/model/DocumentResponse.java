package de.muenchen.refarch.integration.dms.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DocumentResponse {
    private String documentCoo;
    private List<String> contentCoos;
}
