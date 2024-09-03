package de.muenchen.refarch.integration.dms.application.port.in;

import de.muenchen.refarch.integration.dms.domain.exception.DmsException;

public interface SearchSubjectAreaInPort {

    /**
     * Search on a subject scope with optional refinement on a specific business case.
     *
     * @param searchString String to search for
     * @param user         account name
     * @return Subject id.
     */
    String searchSubjectArea(String searchString, String user) throws DmsException;
}
