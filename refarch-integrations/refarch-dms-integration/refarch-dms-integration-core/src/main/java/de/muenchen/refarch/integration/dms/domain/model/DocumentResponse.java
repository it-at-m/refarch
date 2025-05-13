package de.muenchen.refarch.integration.dms.domain.model;

import java.util.List;

public record DocumentResponse(String documentCoo, List<String> contentCoos) {
}
