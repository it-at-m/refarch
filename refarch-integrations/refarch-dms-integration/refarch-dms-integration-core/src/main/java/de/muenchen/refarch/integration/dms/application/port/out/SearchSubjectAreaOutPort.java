package de.muenchen.refarch.integration.dms.application.port.out;

import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import java.util.List;

public interface SearchSubjectAreaOutPort {

    List<String> searchSubjectArea(String searchString, String user) throws DmsException;
}
