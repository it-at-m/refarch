package de.muenchen.refarch.integration.dms.application.usecase;

import de.muenchen.refarch.integration.dms.application.port.in.ReadContentInPort;
import de.muenchen.refarch.integration.dms.application.port.out.ReadContentOutPort;
import de.muenchen.refarch.integration.dms.application.port.out.TransferContentOutPort;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@RequiredArgsConstructor
public class ReadContentUseCase implements ReadContentInPort {

    private final TransferContentOutPort transferContentOutPort;
    private final ReadContentOutPort readContentOutPort;

    @Override
    public void readContent(
            final List<String> contentCoos,
            @NotBlank final String user,
            @NotBlank final String filePath) {
        val content = readContentOutPort.readContent(contentCoos, user);
        transferContentOutPort.transferContent(content, filePath);
    }
}
