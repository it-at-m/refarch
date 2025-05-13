package de.muenchen.refarch.integration.dms.application.usecase;

import de.muenchen.refarch.integration.dms.application.port.in.SearchSubjectAreaInPort;
import de.muenchen.refarch.integration.dms.application.port.out.SearchSubjectAreaOutPort;
import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
@RequiredArgsConstructor
public class SearchSubjectAreaUseCase implements SearchSubjectAreaInPort {

    private final SearchSubjectAreaOutPort searchSubjectAreaOutPort;

    @Override
    public String searchSubjectArea(final String searchString, final String user) throws DmsException {

        final List<String> subjectAreas = searchSubjectAreaOutPort.searchSubjectArea(searchString, user);

        if (subjectAreas.isEmpty()) {
            throw new DmsException("OBJECT_NOT_FOUND", String.format("Subject Area not found with searchString %s and user %s", searchString, user));
        }

        return subjectAreas.getFirst();
    }
}
