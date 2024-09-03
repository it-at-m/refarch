package de.muenchen.refarch.integration.dms.application.port.out;

import de.muenchen.refarch.integration.dms.domain.model.Content;

import java.util.List;

public interface LoadFileOutPort {

    List<Content> loadFiles(List<String> filepaths);

}
