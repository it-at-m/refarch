package de.muenchen.refarch.integration.dms.application.usecase;

import de.muenchen.refarch.integration.dms.application.port.out.SearchSubjectAreaOutPort;
import de.muenchen.refarch.integration.dms.application.port.in.SearchSubjectAreaInPort;
import de.muenchen.oss.digiwf.message.process.api.error.BpmnError;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.validation.annotation.Validated;

@Validated
@RequiredArgsConstructor
public class SearchSubjectAreaUseCase implements SearchSubjectAreaInPort {

    private final SearchSubjectAreaOutPort searchSubjectAreaOutPort;

    @Override
    public String searchSubjectArea(final String searchString, final String user) {

        val subjectAreas = searchSubjectAreaOutPort.searchSubjectArea(searchString, user);

        if (subjectAreas.isEmpty()) {
            throw new BpmnError("OBJECT_NOT_FOUND", String.format("Subject Area not found with searchString %s and user %s", searchString, user));
        }

        // return first result
        return subjectAreas.get(0);
    }
}
