package de.muenchen.refarch.integration.s3.application.usecase;

import de.muenchen.refarch.integration.s3.application.port.in.CreatePresignedUrlsInPort;
import de.muenchen.refarch.integration.s3.domain.exception.FileSystemAccessException;
import de.muenchen.refarch.integration.s3.domain.model.CreatePresignedUrlEvent;
import de.muenchen.refarch.integration.s3.domain.model.PresignedUrl;
import io.minio.http.Method;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

@RequiredArgsConstructor
@Validated
public class CreatePresignedUrlsUseCase implements CreatePresignedUrlsInPort {

    public static final String PATH_DELIMITER = ";";
    private final FileOperationsPresignedUrlUseCase fileHandlingService;
    private final int presignedUrlExpiresInMinutes;

    @Override
    @NonNull
    public List<PresignedUrl> createPresignedUrls(@Valid CreatePresignedUrlEvent event) throws FileSystemAccessException {
        return this.fileHandlingService.getPresignedUrls(
                List.of(event.path().split(PATH_DELIMITER)),
                Method.valueOf(event.action()),
                this.presignedUrlExpiresInMinutes // 7 days is max expiration
        );
    }

}
