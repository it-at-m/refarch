package de.muenchen.oss.refarch.integration.pscd.service.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

/**
 * What the audit trail says: the lines themselves are the deliverable here, so they are asserted.
 */
class PscdInboundAuthenticationLogTest {

    private static final String USERNAME = "pscd-sender";
    // Not an address anything connects to; it is what the test puts in, and asserts comes out.
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    private static final String REMOTE_ADDRESS = "10.1.2.3";

    private final PscdInboundAuthenticationLog authenticationLog = new PscdInboundAuthenticationLog();
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private Logger logger;

    @BeforeEach
    void captureLog() {
        this.appender.start();
        this.logger = (Logger) LoggerFactory.getLogger(PscdInboundAuthenticationLog.class);
        this.logger.addAppender(this.appender);
    }

    @AfterEach
    void releaseLog() {
        this.logger.detachAppender(this.appender);
        this.appender.stop();
    }

    @Test
    void logsASuccessfulAuthenticationWithUserAndOrigin() {
        this.authenticationLog.onSuccess(new AuthenticationSuccessEvent(authentication(USERNAME)));

        assertThat(lines(Level.INFO)).singleElement().satisfies(line -> assertThat(line)
                .contains("authenticated user 'pscd-sender'")
                .contains(REMOTE_ADDRESS));
    }

    /** A rejected credential is a WARN: nobody should have to raise a log level to see it. */
    @Test
    void logsAFailedAuthenticationWithTheReason() {
        this.authenticationLog.onFailure(new AuthenticationFailureBadCredentialsEvent(authentication(USERNAME),
                new BadCredentialsException("Bad credentials")));

        assertThat(lines(Level.WARN)).singleElement().satisfies(line -> assertThat(line)
                .contains("rejected user 'pscd-sender'")
                .contains(REMOTE_ADDRESS)
                .contains("BadCredentialsException"));
    }

    /** The password is in the event; it may never reach a line. */
    @Test
    void neverLogsTheCredential() {
        this.authenticationLog.onFailure(new AuthenticationFailureBadCredentialsEvent(
                UsernamePasswordAuthenticationToken.unauthenticated(USERNAME, "s3cret"),
                new BadCredentialsException("Bad credentials")));

        assertThat(lines(Level.WARN)).singleElement().satisfies(line -> assertThat(line).doesNotContain("s3cret"));
    }

    /** The username comes off a header, so a CR in it must not forge a second line. */
    @Test
    void sanitizesTheAttemptedUsername() {
        this.authenticationLog.onFailure(new AuthenticationFailureBadCredentialsEvent(
                authentication("evil\nPSCD inbound authenticated user 'admin'"),
                new BadCredentialsException("Bad credentials")));

        assertThat(lines(Level.WARN)).singleElement().satisfies(line -> assertThat(line)
                .doesNotContain("\n")
                .contains("evil?PSCD inbound authenticated user 'admin'"));
    }

    @Test
    void logsARequestThatCarriedNoCredentials() {
        this.authenticationLog.rejectedWithoutCredentials(request(null));

        assertThat(lines(Level.WARN)).singleElement().satisfies(line -> assertThat(line)
                .contains("unauthenticated POST /api/pscd/batches")
                .contains(REMOTE_ADDRESS));
    }

    /**
     * A request that did present a Basic header has already been reported by the failure event, and the
     * entry point runs for it as well, so this route stays quiet to keep one rejection to one line.
     */
    @Test
    void staysQuietWhenTheRequestDidPresentBasicCredentials() {
        this.authenticationLog.rejectedWithoutCredentials(request("Basic cHNjZC1zZW5kZXI6d3Jvbmc="));
        // Case-insensitively, as the header scheme is defined.
        this.authenticationLog.rejectedWithoutCredentials(request("basic cHNjZC1zZW5kZXI6d3Jvbmc="));

        assertThat(this.appender.list).isEmpty();
    }

    /** Any other scheme was not something this service could have authenticated, so it is reported. */
    @Test
    void logsARequestWithANonBasicAuthorizationHeader() {
        this.authenticationLog.rejectedWithoutCredentials(request("Bearer some-token"));

        assertThat(lines(Level.WARN)).hasSize(1);
    }

    private static Authentication authentication(final String username) {
        final UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, null,
                AuthorityUtils.createAuthorityList("ROLE_PSCD_SENDER"));
        token.setDetails(new WebAuthenticationDetails(REMOTE_ADDRESS, null));
        return token;
    }

    private static MockHttpServletRequest request(final String authorizationHeader) {
        final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/pscd/batches");
        request.setRemoteAddr(REMOTE_ADDRESS);
        if (authorizationHeader != null) {
            request.addHeader("Authorization", authorizationHeader);
        }
        return request;
    }

    private List<String> lines(final Level level) {
        return this.appender.list.stream()
                .filter(event -> level.equals(event.getLevel()))
                .map(ILoggingEvent::getFormattedMessage)
                .toList();
    }
}
