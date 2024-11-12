package de.muenchen.refarch.integration.cosys.application.port.out;

import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;

public interface SaveFileToStorageOutPort {
    void saveDocumentInStorage(
            String filePath,
            byte[] data) throws DocumentStorageException;
}
