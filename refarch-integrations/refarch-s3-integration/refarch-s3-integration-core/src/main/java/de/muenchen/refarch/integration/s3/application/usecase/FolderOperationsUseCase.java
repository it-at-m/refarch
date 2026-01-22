package de.muenchen.refarch.integration.s3.application.usecase;

import de.muenchen.refarch.integration.s3.application.port.in.FolderOperationsInPort;
import de.muenchen.refarch.integration.s3.application.port.out.S3OutPort;
import de.muenchen.refarch.integration.s3.domain.exception.S3Exception;
import de.muenchen.refarch.integration.s3.domain.model.FileMetadata;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link FolderOperationsInPort} delegating to {@link S3OutPort}.
 */
@RequiredArgsConstructor
public class FolderOperationsUseCase implements FolderOperationsInPort {
    private final S3OutPort s3OutPort;

    @Override
    public List<FileMetadata> getFilesInFolder(final String bucket, final String pathToFolder, final boolean recursive) throws S3Exception {
        final List<FileMetadata> all = s3OutPort.getFilesWithPrefix(bucket, pathToFolder, 1000, null);
        if (recursive) {
            return all;
        }
        final String normalizedPrefix = ensureTrailingSlash(pathToFolder);
        return all.stream()
                .filter(meta -> isImmediateChild(normalizedPrefix, meta.path()))
                .collect(Collectors.toList());
    }

    private static String ensureTrailingSlash(final String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return "";
        }
        return prefix.endsWith("/") ? prefix : prefix + "/";
    }

    private static boolean isImmediateChild(final String prefix, final String path) {
        if (!path.startsWith(prefix)) {
            return false;
        }
        final String remainder = path.substring(prefix.length());
        return !remainder.contains("/");
    }
}
