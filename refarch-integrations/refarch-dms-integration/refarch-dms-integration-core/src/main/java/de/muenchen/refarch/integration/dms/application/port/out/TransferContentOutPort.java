package de.muenchen.refarch.integration.dms.application.port.out;

import de.muenchen.refarch.integration.dms.domain.model.Content;

import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import java.util.List;

public interface TransferContentOutPort {

    void transferContent(List<Content> content, String filepath) throws DocumentStorageException;

}
