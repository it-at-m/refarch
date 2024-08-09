package de.muenchen.refarch.integration.s3.application.port.in;

import de.muenchen.refarch.integration.s3.domain.exception.FileSystemAccessException;
import de.muenchen.refarch.integration.s3.domain.model.CreatePresignedUrlEvent;
import de.muenchen.refarch.integration.s3.domain.model.PresignedUrl;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.lang.NonNull;

public interface CreatePresignedUrlsInPort {
    /**
     * Create pre-signed URLs.
     *
     * @param event event containing the request.
     * @return resulting variable map.
     * @throws FileSystemAccessException on S3 errors.
     * @throws javax.validation.ConstraintViolationException if the request is not valid.
     */
    @NonNull
    List<PresignedUrl> createPresignedUrls(@Valid CreatePresignedUrlEvent event) throws FileSystemAccessException;
}
