package de.muenchen.refarch.s3.integration.java;

import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFileJavaRepository;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFolderJavaRepository;
import de.muenchen.refarch.s3.integration.S3ExampleService;
import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class JavaExampleService extends S3ExampleService {

    public JavaExampleService(final DocumentStorageFileJavaRepository fileRepository,
            final DocumentStorageFolderJavaRepository folderRepository) {
        super(fileRepository, folderRepository, "java");
    }

    @Override
    public void testS3() throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException, IOException {
        super.testS3();
    }
}
