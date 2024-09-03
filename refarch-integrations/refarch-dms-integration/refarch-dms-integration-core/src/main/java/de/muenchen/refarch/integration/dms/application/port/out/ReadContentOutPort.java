package de.muenchen.refarch.integration.dms.application.port.out;

import de.muenchen.refarch.integration.dms.domain.model.Content;

import java.util.List;

public interface ReadContentOutPort {

    List<Content> readContent(final List<String> coos, final String user);

}
