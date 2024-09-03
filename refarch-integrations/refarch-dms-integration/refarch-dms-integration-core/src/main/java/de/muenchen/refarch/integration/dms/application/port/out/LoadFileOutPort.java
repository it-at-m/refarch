package de.muenchen.refarch.integration.dms.application.port.out;

import de.muenchen.refarch.integration.dms.domain.Content;

import java.util.List;

public interface LoadFileOutPort {

    List<Content> loadFiles(List<String> filepaths, String fileContext, String processDefinition);

}
