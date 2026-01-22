package de.muenchen.refarch.integration.s3.application.usecase;

import de.muenchen.refarch.integration.s3.application.port.in.FileOperationsInPort;
import de.muenchen.refarch.integration.s3.application.port.out.S3OutPort;
import de.muenchen.refarch.integration.s3.domain.exception.S3Exception;
import de.muenchen.refarch.integration.s3.domain.model.FileMetadata;
import de.muenchen.refarch.integration.s3.domain.model.FileReference;
import de.muenchen.refarch.integration.s3.domain.model.PresignedUrl;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

/**
 * Default implementation of {@link FileOperationsInPort} delegating to {@link S3OutPort}.
 */
@RequiredArgsConstructor
public class FileOperationsUseCase implements FileOperationsInPort {

    private final S3OutPort s3OutPort;

    @Override
    public boolean fileExists(final FileReference fileReference) throws S3Exception {
        return s3OutPort.fileExists(fileReference);
    }

    @Override
    public void saveFile(final FileReference fileReference, final InputStream content, final long contentLength) throws S3Exception {
        s3OutPort.saveFile(fileReference, content, contentLength);
    }

    @Override
    public void saveFile(final FileReference fileReference, final InputStream content) throws S3Exception {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("s3-upload-", ".tmp");
            Files.copy(content, tempFile, StandardCopyOption.REPLACE_EXISTING);
            final long inferredLength = Files.size(tempFile);

            try (FileInputStream fis = new FileInputStream(tempFile.toFile())) {
                s3OutPort.saveFile(fileReference, fis, inferredLength);
            }
        } catch (IOException e) {
            throw new S3Exception("Failed to stream and upload file: " + fileReference, e);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                    // best-effort cleanup
                }
            }
        }
    }

    @Override
    public void saveFile(final FileReference fileReference, final File file) throws S3Exception {
        s3OutPort.saveFile(fileReference, file);
    }

    @Override
    public FileMetadata getFileMetadata(final FileReference fileReference) throws S3Exception {
        return s3OutPort.getFileMetadata(fileReference);
    }

    @Override
    public PresignedUrl getPresignedUrl(final FileReference fileReference, final PresignedUrl.Action action, final Duration lifetime) throws S3Exception {
        return s3OutPort.getPresignedUrl(fileReference, action, lifetime);
    }

    @Override
    public InputStream getFileContent(final FileReference fileReference) throws S3Exception {
        return s3OutPort.getFileContent(fileReference);
    }

    @Override
    public void deleteFile(final FileReference fileReference) throws S3Exception {
        s3OutPort.deleteFile(fileReference);
    }

}
