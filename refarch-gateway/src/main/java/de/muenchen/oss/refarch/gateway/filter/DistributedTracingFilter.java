package de.muenchen.oss.refarch.gateway.filter;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/// This class adds the sleuth headers `X-B3-SpanId` and `X-B3-TraceId` to each route response.
@Component
@Slf4j
@RequiredArgsConstructor
public class DistributedTracingFilter implements WebFilter {

    public static final String TRACE_ID = "TraceId";
    public static final String SPAN_ID = "SpanId";

    private final Tracer tracer;

    /// This method adds the Zipkin headers `X-B3-SpanId` and `X-B3-TraceId` to each response in
    /// [ServerWebExchange].
    ///
    /// @param serverWebExchange the current server exchange without Zipkin headers
    /// @param webFilterChain provides a way to delegate to the next filter
    /// @return `Mono<Void>` to indicate when request processing for adding Zipkin headers is
    ///         complete
    @Override
    @NonNull public Mono<Void> filter(final ServerWebExchange serverWebExchange,
            final WebFilterChain webFilterChain) {
        final ServerHttpResponse response = serverWebExchange.getResponse();
        response.beforeCommit(() -> {
            final Span span = tracer.currentSpan();
            if (span != null) {
                final HttpHeaders headers = response.getHeaders();
                headers.add(TRACE_ID, span.context().traceId());
                headers.add(SPAN_ID, span.context().spanId());
            } else {
                log.debug("Traceinformation missing - Skip Trace Header insertion");
            }
            return Mono.empty();
        });
        return webFilterChain.filter(serverWebExchange);
    }

}
