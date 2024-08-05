/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik der Landeshauptstadt München, 2020
 */

package de.muenchen.refarch.spring.security.authentication;

import java.util.Set;
import org.springframework.lang.NonNull;

/**
 * Provides the username for the currently logged-in user.
 */
public interface UserAuthenticationProvider {

    /**
     * Get the user id of the logged-in user.
     *
     * @return user id.
     */
    @NonNull
    String getLoggedInUser();

    /**
     * Retrieves user roles of logged-in user.
     *
     * @return set of users.
     */
    @NonNull
    Set<String> getLoggedInUserRoles();
}
