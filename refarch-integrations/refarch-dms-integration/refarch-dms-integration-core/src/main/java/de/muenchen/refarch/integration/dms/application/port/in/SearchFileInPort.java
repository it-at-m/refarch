package de.muenchen.refarch.integration.dms.application.port.in;

import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import java.util.List;

@SuppressWarnings("PMD.UseObjectForClearerAPI")
public interface SearchFileInPort {

    /**
     * Searches for one or multiple files. A Search can be refined by an optional 'Fachdatum'/business
     * case.
     *
     * @param searchString String to search for
     * @param user account name
     * @param reference (optional) 'Fachdatum'/business case to refine a search
     * @param value (optional) value of 'Fachdatum'/business case
     * @return List of file ids.
     */
    List<String> searchFile(String searchString, String user, String reference, String value) throws DmsException;
}
