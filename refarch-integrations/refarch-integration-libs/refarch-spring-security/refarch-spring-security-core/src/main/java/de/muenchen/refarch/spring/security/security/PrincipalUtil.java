package de.muenchen.refarch.spring.security.security;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.security.Principal;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class PrincipalUtil {
    @SuppressWarnings("unused")
    public static List<String> extractRoles(Principal principal) {
        if (principal instanceof Authentication) {
            return extractRoles((Authentication) principal);
        }
        return emptyList();
    }

    public static List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> StringUtils.removeStart(role, SecurityConfiguration.SPRING_ROLE_PREFIX))
                .collect(toList());
    }
}
