package de.muenchen.refarch.integration.cosys.application.port.out;

import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;

public interface SaveFileToStorageOutPort {
    void saveDocumentInStorage(
            final String filePath,
            final byte[] data) throws DocumentStorageException;
}
