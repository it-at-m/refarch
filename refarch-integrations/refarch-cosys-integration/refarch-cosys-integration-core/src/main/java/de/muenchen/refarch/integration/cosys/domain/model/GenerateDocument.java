package de.muenchen.refarch.integration.cosys.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * Dto for generating documents.
 *
 * @param client Client that is used in cosys
 * @param role Role that is used in cosys
 * @param guid The GUID of the target template to be filled
 * @param variables All data to be filled into template
 */
@Builder
public record GenerateDocument(
        @NotBlank(message = "client is mandatory") String client,
        @NotBlank(message = "role is mandatory") String role,
        @NotBlank(message = "guid is mandatory") String guid,
        JsonNode variables) {
}
