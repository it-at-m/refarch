package de.muenchen.oss.digiwf.cosys.integration.application.port.out;

import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;

public interface SaveFileToStorageOutPort {
    void saveDocumentInStorage(
            final String filePath,
            final byte[] data
    ) throws DocumentStorageException;
}
