package de.muenchen.refarch.integration.dms.application.port.out;

import de.muenchen.refarch.integration.dms.domain.Procedure;

public interface CreateProcedureOutPort {

    Procedure createProcedure(Procedure procedure, String user);

}
