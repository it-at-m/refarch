package de.muenchen.oss.refarch.integration.dms.domain.model;

import java.util.Collections;
import java.util.List;

public record DocumentResponse(String documentCoo, List<String> contentCoos) {

    public List<String> contentCoos() {
        return Collections.unmodifiableList(this.contentCoos);
    }

}
