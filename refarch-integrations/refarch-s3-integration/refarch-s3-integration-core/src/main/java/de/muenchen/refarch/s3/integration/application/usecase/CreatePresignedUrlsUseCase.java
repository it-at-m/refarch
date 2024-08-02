package de.muenchen.refarch.s3.integration.application.usecase;

import de.muenchen.refarch.s3.integration.application.port.in.CreatePresignedUrlsInPort;
import de.muenchen.refarch.s3.integration.domain.exception.FileSystemAccessException;
import de.muenchen.refarch.s3.integration.domain.model.CreatePresignedUrlEvent;
import de.muenchen.refarch.s3.integration.domain.model.PresignedUrl;
import io.minio.http.Method;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

@RequiredArgsConstructor
@Validated
public class CreatePresignedUrlsUseCase implements CreatePresignedUrlsInPort {

    private final FileOperationsPresignedUrlUseCase fileHandlingService;
    private final int presignedUrlExpiresInMinutes;

    @Override
    @NonNull
    public List<PresignedUrl> createPresignedUrls(@Valid CreatePresignedUrlEvent event) throws FileSystemAccessException {
        return this.fileHandlingService.getPresignedUrls(
                List.of(event.getPath().split(";")),
                Method.valueOf(event.getAction()),
                this.presignedUrlExpiresInMinutes // 7 days is max expiration
        );
    }

}
