package de.muenchen.refarch.integration.dms.adapter.out.fabasoft.auth;

import de.muenchen.refarch.integration.dms.application.port.out.DmsUserOutPort;
import de.muenchen.oss.digiwf.spring.security.authentication.UserAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DmsUserAdapter implements DmsUserOutPort {

    private final UserAuthenticationProvider userAuthenticationProvider;

    @Override
    public String getDmsUser() {
        return userAuthenticationProvider.getLoggedInUser();
    }
}
