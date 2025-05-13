package de.muenchen.refarch.integration.dms.application.usecase;

import de.muenchen.refarch.integration.dms.application.port.in.ReadContentInPort;
import de.muenchen.refarch.integration.dms.application.port.out.ReadContentOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.TransferContentOutPort;
import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Content;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
@RequiredArgsConstructor
public class ReadContentUseCase implements ReadContentInPort {

    private final TransferContentOutPort transferContentOutPort;
    private final ReadContentOutPort readContentOutPort;

    @Override
    public void readContent(
            final List<String> contentCoos,
            @NotBlank final String user,
            @NotBlank final String filePath) throws DmsException, DocumentStorageException {
        final List<Content> content = readContentOutPort.readContent(contentCoos, user);
        transferContentOutPort.transferContent(content, filePath);
    }
}
