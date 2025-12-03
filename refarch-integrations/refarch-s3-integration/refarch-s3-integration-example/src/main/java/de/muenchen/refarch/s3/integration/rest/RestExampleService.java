package de.muenchen.refarch.s3.integration.rest;

import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFileRestRepository;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFolderRestRepository;
import de.muenchen.refarch.s3.integration.S3ExampleService;
import java.io.IOException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("rest")
public class RestExampleService extends S3ExampleService {

    public RestExampleService(final DocumentStorageFileRestRepository fileRepository,
            final DocumentStorageFolderRestRepository folderRepository) {
        super(fileRepository, folderRepository, "rest");
    }

    @Override
    public void testS3() throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException, IOException {
        super.testS3();
    }
}
