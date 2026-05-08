package de.muenchen.oss.refarch.integration.dms.domain.model;

import jakarta.validation.constraints.NotBlank;

public record File(@NotBlank String apentryCOO, @NotBlank String title) {
}
