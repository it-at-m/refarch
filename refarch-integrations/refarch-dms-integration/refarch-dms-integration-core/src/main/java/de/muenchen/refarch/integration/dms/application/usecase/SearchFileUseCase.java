package de.muenchen.refarch.integration.dms.application.usecase;

import de.muenchen.refarch.integration.dms.application.port.in.SearchFileInPort;
import de.muenchen.refarch.integration.dms.application.port.out.SearchFileOutPort;
import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
@RequiredArgsConstructor
@SuppressWarnings("PMD.UseObjectForClearerAPI")
public class SearchFileUseCase implements SearchFileInPort {

    private final SearchFileOutPort searchFileOutPort;

    @Override
    public List<String> searchFile(final String searchString, final String user, final String reference, final String value) throws DmsException {

        final List<String> files = searchFileOutPort.searchFile(searchString, user, reference, value);

        if (files.isEmpty()) {
            throw new DmsException("OBJECT_NOT_FOUND", String.format("File not found with searchString %s and user %s", searchString, user));
        }

        return files;
    }
}
