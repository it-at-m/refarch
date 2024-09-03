package de.muenchen.refarch.integration.dms.application.port.out;

import de.muenchen.refarch.integration.dms.domain.model.File;

public interface CreateFileOutPort {

    String createFile(File file, String user);

}
