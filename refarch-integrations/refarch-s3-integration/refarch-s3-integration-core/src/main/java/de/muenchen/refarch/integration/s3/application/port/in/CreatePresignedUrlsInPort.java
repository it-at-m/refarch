package de.muenchen.refarch.integration.s3.application.port.in;

import de.muenchen.refarch.integration.s3.domain.exception.FileSystemAccessException;
import de.muenchen.refarch.integration.s3.domain.model.CreatePresignedUrlEvent;
import de.muenchen.refarch.integration.s3.domain.model.PresignedUrl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public interface CreatePresignedUrlsInPort {
    /**
     * Create pre-signed URLs.
     *
     * @param event event containing the request.
     * @return resulting variable map.
     * @throws FileSystemAccessException on S3 errors.
     * @throws jakarta.validation.ConstraintViolationException if the request is not valid.
     */
    @NotNull List<PresignedUrl> createPresignedUrls(@Valid CreatePresignedUrlEvent event) throws FileSystemAccessException;
}
