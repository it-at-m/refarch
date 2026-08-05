package de.muenchen.oss.refarch.gateway.configuration;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.security.web.server.csrf.CsrfWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

@Configuration
@Profile("!no-security")
public class CsrfProtectionMatcher implements ServerWebExchangeMatcher {

    /**
     * Copied from {@link CsrfWebFilter}.DefaultRequireCsrfProtectionMatcher
     */
    private static final Set<HttpMethod> ALLOWED_METHODS = new HashSet<>(
            Arrays.asList(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.TRACE, HttpMethod.OPTIONS));

    private final List<PathPattern> whitelistPatterns;

    @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
    public CsrfProtectionMatcher(final SecurityProperties securityProperties) {
        final PathPatternParser parser = new PathPatternParser();

        whitelistPatterns = securityProperties.getCsrfWhitelisted().stream()
                .map(parser::parse)
                .toList();
    }

    @Override
    @NonNull public Mono<MatchResult> matches(final ServerWebExchange exchange) {
        return Mono.just(exchange.getRequest())
                .flatMap((r) -> Mono.justOrEmpty(new MethodAndPath(r.getMethod(), r.getPath().toString())))
                .filter((mp) -> ALLOWED_METHODS.contains(mp.method) || isWhitelisted(mp.path))
                .flatMap((m) -> MatchResult.notMatch())
                .switchIfEmpty(MatchResult.match());
    }

    private boolean isWhitelisted(final String path) {
        return whitelistPatterns.stream()
                .anyMatch(pattern -> pattern.matches(PathContainer.parsePath(path)));
    }

    private record MethodAndPath(HttpMethod method, String path) {
    }
}
