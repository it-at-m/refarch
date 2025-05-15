package de.muenchen.refarch.integration.cosys.adapter.out.cosys;

import org.springframework.core.io.ByteArrayResource;

/**
 * {@link ByteArrayResource} with filename to allow content type detection on receiving server.
 */
public class NamedByteArrayResource extends ByteArrayResource {
    final private String filename;

    public NamedByteArrayResource(final byte[] byteArray, final String filename) {
        super(byteArray);
        this.filename = filename;
    }

    @Override
    public String getFilename() {
        return this.filename;
    }
}
