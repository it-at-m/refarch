package de.muenchen.refarch.integration.cosys.adapter.out.cosys;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileUtils {
    private FileUtils() {
    }

    public static File createFile(final String name, final byte[] content) throws IOException {
        final Path tempFile = Files.createTempFile(name, ".json");
        Files.write(tempFile, content);
        return tempFile.toFile();
    }
}
