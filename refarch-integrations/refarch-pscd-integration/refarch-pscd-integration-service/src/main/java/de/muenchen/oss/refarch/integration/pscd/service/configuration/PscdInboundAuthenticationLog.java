package de.muenchen.oss.refarch.integration.pscd.service.configuration;

import de.muenchen.oss.refarch.integration.pscd.service.adapter.in.PscdBatchLog;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

/**
 * The audit trail for the inbound HTTP channels: who authenticated, who failed, and from where.
 *
 * <p>
 * A batch is accepted fire-and-forget, so these lines are the only record of which sender submitted
 * what. Successes are {@code INFO}, roughly one per submission, next to the batch's own accepted
 * line, and everything rejected is {@code WARN}, because on a machine-to-machine interface a failed
 * authentication is a misconfigured sender or someone guessing, and neither should need a log level
 * raised to become visible.
 * </p>
 *
 * <p>
 * Rejections arrive by two routes, which is why this class has both event listeners and the
 * {@link #rejectedWithoutCredentials} hook. Spring Security only publishes an authentication event
 * when credentials were actually presented and found wanting; a request with no
 * {@code Authorization} header at all never reaches an authentication provider, so the entry point
 * reports that case instead. Without the second route the most common failure, a sender that sends
 * nothing at all, would be invisible.
 * </p>
 *
 * <p>
 * The username is attacker-controlled, so it is sanitized before it goes into a line; the password
 * never appears, although the event carries it.
 * </p>
 */
@Component
@Slf4j
public class PscdInboundAuthenticationLog {

    private static final String UNKNOWN_ORIGIN = "<unknown>";

    @EventListener
    public void onSuccess(final AuthenticationSuccessEvent event) {
        log.info("PSCD inbound authenticated user '{}' from {}",
                safe(event.getAuthentication().getName()), origin(event.getAuthentication()));
    }

    /**
     * Every flavour of failure (wrong password, unknown user, locked or disabled account) arrives as
     * a subclass of this one event, so one listener covers them all and names which it was.
     */
    @EventListener
    public void onFailure(final AbstractAuthenticationFailureEvent event) {
        log.warn("PSCD inbound rejected user '{}' from {}: {}",
                safe(event.getAuthentication().getName()), origin(event.getAuthentication()),
                event.getException().getClass().getSimpleName());
    }

    /**
     * Report a request that carried no credentials at all. Called from the authentication entry point,
     * which also runs after a failed authentication, hence the guard: a request that did present a
     * {@code Basic} header has already been logged by {@link #onFailure}, and reporting it twice would
     * make the trail harder to read, not easier.
     *
     * @param request the rejected request
     */
    public void rejectedWithoutCredentials(final HttpServletRequest request) {
        final String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.regionMatches(true, 0, "Basic ", 0, "Basic ".length())) {
            return;
        }
        log.warn("PSCD inbound rejected an unauthenticated {} {} from {}",
                safe(request.getMethod()), safe(request.getRequestURI()), request.getRemoteAddr());
    }

    /** Where the request came from, as the servlet container saw it. */
    private static String origin(final Authentication authentication) {
        return authentication.getDetails() instanceof final WebAuthenticationDetails details
                ? details.getRemoteAddress()
                : UNKNOWN_ORIGIN;
    }

    /**
     * Make header-supplied text safe to put in a log line. Reuses the inbound adapters' sanitizer: the
     * concern is the same one, untrusted content forging additional lines.
     */
    private static String safe(final String value) {
        return PscdBatchLog.safeFilename(value);
    }
}
