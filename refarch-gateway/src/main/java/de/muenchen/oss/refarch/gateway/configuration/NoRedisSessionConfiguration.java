package de.muenchen.oss.refarch.gateway.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.session.autoconfigure.SessionTimeout;
import org.springframework.boot.session.data.redis.autoconfigure.SessionDataRedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.web.server.session.DefaultWebSessionManager;
import org.springframework.web.server.session.InMemoryWebSessionStore;
import org.springframework.web.server.session.WebSessionManager;

/**
 * Configuration that disables Redis-backed session autoconfiguration when the {@code redis-session}
 * Spring profile is not active. In this case an in-memory web session storage is used.
 * The timeout settings are configured to mimic those of the autoconfiguration
 * {@link SessionDataRedisAutoConfiguration}.
 */
@Configuration
@Profile("!redis-session")
@EnableAutoConfiguration(exclude = SessionDataRedisAutoConfiguration.class)
@Slf4j
@RequiredArgsConstructor
public class NoRedisSessionConfiguration {

    final SessionTimeout sessionTimeout;

    @EventListener(ApplicationReadyEvent.class)
    public void redisInfoLogger() {
        log.warn("Redis is currently not used for session sharing but recommended for production use. Enable it via the `redis-session` Spring profile.");
    }

    /**
     * This bean is required to be able to set {@link org.springframework.web.server.WebSession} timeout
     * using {@code spring.session.timeout} property when no Redis is in use.
     *
     * @return In-memory-based {@link WebSessionManager} configured using {@link SessionTimeout}
     */
    @Bean
    WebSessionManager webSessionManager() {
        final InMemoryWebSessionStore sessionStore = new InMemoryWebSessionStore();

        if (sessionTimeout.getTimeout() != null) {
            sessionStore.setDefaultMaxIdleTime(sessionTimeout.getTimeout());
        }

        final DefaultWebSessionManager manager = new DefaultWebSessionManager();
        manager.setSessionStore(sessionStore);
        return manager;
    }

}
