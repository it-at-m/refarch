package de.muenchen.refarch.integration.dms.application.port.out;

import de.muenchen.refarch.integration.dms.domain.exception.DmsException;

public interface CancelObjectOutPort {

    void cancelObject(String objectCoo, String user) throws DmsException;

}
