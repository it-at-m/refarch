package de.muenchen.oss.refarch.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/// This class subscribes the [ServerWebExchange] for CSRF token attachment within the classes
/// [CookieServerCsrfTokenRepository] and [CsrfWebFilter]. The CSRF configuration done only in
/// [SecurityConfiguration#springSecurityFilterChain] is not sufficient for CSRF token attachment to
/// a [ServerHttpResponse].
@Component
@Slf4j
public class CsrfTokenAppendingHelperFilter implements WebFilter {

    @Override
    @NonNull public Mono<Void> filter(final ServerWebExchange exchange, final WebFilterChain chain) {
        log.debug("Trigger to append CSRF token to response");
        final Mono<CsrfToken> csrfToken = exchange.getAttributeOrDefault(CsrfToken.class.getName(), Mono.empty());
        return csrfToken.doOnSuccess(_ -> {
            // do nothing -> CSRF-Token is added as cookie in class CookieServerCsrfTokenRepository#saveToken
        }).then(chain.filter(exchange));
    }

}
