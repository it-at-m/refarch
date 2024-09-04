package de.muenchen.oss.digiwf.cosys.integration.adapter.out.s3;

import de.muenchen.oss.digiwf.cosys.integration.application.port.out.SaveFileToStorageOutPort;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.client.exception.FileSizeValidationException;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFileRepository;
import de.muenchen.refarch.integration.s3.client.repository.transfer.S3FileTransferRepository;
import de.muenchen.refarch.integration.s3.client.service.FileValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.unit.DataSize;

@Slf4j
@RequiredArgsConstructor
public class S3Adapter implements SaveFileToStorageOutPort {

    private static final String S3_FILE_SIZE_ERROR = "S3_FILE_SIZE_ERROR";
    private final S3FileTransferRepository s3FileTransferRepository;
    private final DocumentStorageFileRepository documentStorageFileRepository;
    private final FileValidationService fileValidationService;

    @Override
    public void saveDocumentInStorage(
            final String filePath,
            final byte[] data
    ) throws DocumentStorageException {
        try {
            validateFileSize(data);
            documentStorageFileRepository.saveFile(filePath, data, 1);
        } catch (DocumentStorageException | DocumentStorageClientErrorException |
                DocumentStorageServerErrorException e) {
            throw new DocumentStorageException("Document could not be saved.", e);
        }
    }

    private void validateFileSize(final byte[] data) {
        if (!fileValidationService.isValidFileSize(data))
            throw new FileSizeValidationException(
                    String.format("Invalid file size %d MB. Allowed are %d MB.", DataSize.ofBytes(data.length).toMegabytes(),
                            fileValidationService.getMaxFileSize().toMegabytes()));
    }
}
