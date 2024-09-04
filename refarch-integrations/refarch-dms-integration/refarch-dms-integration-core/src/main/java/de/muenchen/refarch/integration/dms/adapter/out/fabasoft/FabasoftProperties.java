/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik der Landeshauptstadt München, 2020
 */
package de.muenchen.refarch.integration.dms.adapter.out.fabasoft;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "refarch.dms.fabasoft")
public class FabasoftProperties {

    @NotBlank
    private String url;

    @NotBlank
    private String username;

    private String password;

    private String businessapp;

    private String uiUrl;

    private Boolean enableMtom;
}
