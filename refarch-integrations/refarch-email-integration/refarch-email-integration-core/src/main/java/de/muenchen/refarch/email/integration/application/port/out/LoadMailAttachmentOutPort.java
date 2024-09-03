package de.muenchen.refarch.email.integration.application.port.out;

import de.muenchen.refarch.email.model.FileAttachment;
import java.util.List;

public interface LoadMailAttachmentOutPort {
    List<FileAttachment> loadAttachments(final List<String> filePaths);
}
