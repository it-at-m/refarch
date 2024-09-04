package de.muenchen.oss.digiwf.cosys.integration.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Deprecated
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class DocumentStorageUrl {

    /**
     * Url to the s3 service.
     */
    @NotBlank(message = "Presigned Url is mandatory")
    private String url;

    /**
     * Path to the file inside in the S3 storage.
     */
    @NotBlank(message = "Path is mandatory")
    private String path;

    /**
     * Proper Http Method (Post, Put, Get, Delete) to interact with S3.
     * Note: Only POST and PUT is supported.
     */
    @NotBlank(message = "Action is mandatory")
    @Pattern(regexp = "^(POST|PUT)$", message = "Only action POST or PUT is supported")
    private String action;

}
