package de.muenchen.refarch.integration.dms.application.port.out;

import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Metadata;

public interface ReadMetadataOutPort {

    Metadata readMetadata(String coo, String user) throws DmsException;

    Metadata readContentMetadata(String coo, String user) throws DmsException;

}
