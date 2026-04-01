package de.muenchen.oss.refarch.integration.dms.domain.model;

import jakarta.validation.constraints.NotBlank;

public record RequestContext(@NotBlank String user,
        String jobOe,
        String jobPosition) {
}
