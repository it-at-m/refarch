package de.muenchen.oss.refarch.gateway.exception;

import de.muenchen.oss.refarch.gateway.filter.RedisSessionUnavailableWebFilter;
import java.io.Serial;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Used in {@link RedisSessionUnavailableWebFilter} to signal a downtime of the Redis cache
 * used for session storage.
 */
@ResponseStatus(
        code = HttpStatus.SERVICE_UNAVAILABLE,
        reason = "Redis service unavailable"
)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RedisSessionUnavailableException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
}
