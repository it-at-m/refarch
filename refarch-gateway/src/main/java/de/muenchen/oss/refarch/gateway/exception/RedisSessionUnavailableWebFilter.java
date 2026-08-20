package de.muenchen.oss.refarch.gateway.exception;

import io.lettuce.core.RedisException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Catches Redis session store failures before Spring Security turns them into 500 responses.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RedisSessionUnavailableWebFilter implements WebFilter {

    private static final String SERVICE_UNAVAILABLE_ERROR = "{ \"status\":503, \"error\":\"Service Unavailable\" }";

    @Override
    @NonNull public Mono<Void> filter(@NonNull final ServerWebExchange exchange, @NonNull final WebFilterChain chain) {
        return chain.filter(exchange)
                .onErrorResume(ex -> handleRedisSessionUnavailable(exchange, ex));
    }

    private Mono<Void> handleRedisSessionUnavailable(final ServerWebExchange exchange, final Throwable ex) {
        if (!isRedisSessionUnavailable(ex)) {
            return Mono.error(ex);
        }

        final ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        final ServerHttpRequest request = exchange.getRequest();
        log.warn("Redis session store unavailable: requestId: {}, method: {}, url: {}, error: {}", request.getId(), request.getMethod(), request.getURI(),
                ex.toString());

        return writeTo(response);
    }

    private static boolean isRedisSessionUnavailable(final Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof RedisConnectionFailureException
                    || current instanceof RedisSystemException
                    || current instanceof RedisException
                    || current instanceof DataAccessResourceFailureException
                    || current instanceof QueryTimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static Mono<Void> writeTo(final ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        final DataBuffer dataBuffer = response.bufferFactory().wrap(SERVICE_UNAVAILABLE_ERROR.getBytes(StandardCharsets.UTF_8));
        response.getHeaders().setContentLength(dataBuffer.readableByteCount());
        return response.writeWith(Mono.just(dataBuffer));
    }

}
