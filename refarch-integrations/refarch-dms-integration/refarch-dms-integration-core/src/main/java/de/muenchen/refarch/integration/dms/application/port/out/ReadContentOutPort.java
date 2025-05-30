package de.muenchen.refarch.integration.dms.application.port.out;

import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Content;
import java.util.List;

public interface ReadContentOutPort {

    List<Content> readContent(List<String> coos, String user) throws DmsException;

}
