package de.muenchen.oss.refarch.integration.dms.domain.model;

import java.util.List;

public record DocumentResponse(String documentCoo, List<String> contentCoos) {
}
