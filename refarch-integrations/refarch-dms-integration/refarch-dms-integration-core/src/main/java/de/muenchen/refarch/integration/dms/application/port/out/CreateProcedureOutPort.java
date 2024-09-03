package de.muenchen.refarch.integration.dms.application.port.out;

import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Procedure;

public interface CreateProcedureOutPort {

    Procedure createProcedure(Procedure procedure, String user) throws DmsException;

}
