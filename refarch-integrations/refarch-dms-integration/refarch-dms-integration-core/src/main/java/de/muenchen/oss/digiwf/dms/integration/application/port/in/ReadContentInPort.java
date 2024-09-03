package de.muenchen.oss.digiwf.dms.integration.application.port.in;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public interface ReadContentInPort {

    void readContent(List<String> fileCoos, @NotBlank String user, @NotBlank String filePath, @NotBlank String fileContext,
            @NotBlank String processDefinitionId);

}
