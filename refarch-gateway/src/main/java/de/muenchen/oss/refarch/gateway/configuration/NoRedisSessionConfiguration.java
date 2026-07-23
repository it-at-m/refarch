package de.muenchen.oss.refarch.gateway.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.session.data.redis.autoconfigure.SessionDataRedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration that disables Redis-backed session sharing when the {@code redis-session}
 * Spring profile is not active.
 */
@Configuration
@Profile("!redis-session")
@EnableAutoConfiguration(exclude = SessionDataRedisAutoConfiguration.class)
@Slf4j
public class NoRedisSessionConfiguration {
    @Bean
    public ApplicationRunner redisInfoLogger() {
        return args -> log
                .warn("Redis is currently not used for session sharing but recommended for production use. Enable it via the `redis-session` Spring profile.");
    }
}
