package de.muenchen.refarch.integration.dms.application.port.out;

import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import java.util.List;

public interface SearchFileOutPort {

    List<String> searchFile(String searchString, String user, String reference, String value) throws DmsException;
}
