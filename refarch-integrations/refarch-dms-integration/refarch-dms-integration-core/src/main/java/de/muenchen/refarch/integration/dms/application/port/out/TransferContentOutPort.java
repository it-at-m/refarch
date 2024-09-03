package de.muenchen.refarch.integration.dms.application.port.out;

import de.muenchen.refarch.integration.dms.domain.model.Content;

import java.util.List;

public interface TransferContentOutPort {

    void transferContent(List<Content> content, String filepath, String fileContext, String processDefinition);

}
