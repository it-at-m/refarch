package de.muenchen.oss.refarch.gateway;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

@TestConfiguration
public class SessionAccessConfiguration {

    @Bean
    @Order(0)
    WebFilter sessionAccessFilter() {
        return (final ServerWebExchange exchange, final WebFilterChain chain) -> exchange.getSession()
                .doOnNext(session -> session.getAttributes().put("test", "value"))
                .then(chain.filter(exchange));
    }

}
