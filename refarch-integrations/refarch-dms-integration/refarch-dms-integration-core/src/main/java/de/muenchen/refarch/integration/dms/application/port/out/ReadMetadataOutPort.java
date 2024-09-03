package de.muenchen.refarch.integration.dms.application.port.out;

import de.muenchen.refarch.integration.dms.domain.model.Metadata;

public interface ReadMetadataOutPort {

    Metadata readMetadata(final String coo, final String user);

    Metadata readContentMetadata(final String coo, final String user);

}
