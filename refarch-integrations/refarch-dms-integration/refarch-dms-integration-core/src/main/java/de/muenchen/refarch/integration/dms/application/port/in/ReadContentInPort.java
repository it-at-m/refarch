package de.muenchen.refarch.integration.dms.application.port.in;

import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public interface ReadContentInPort {

    void readContent(List<String> fileCoos, @NotBlank String user, @NotBlank String filePath) throws DmsException, DocumentStorageException;

}
