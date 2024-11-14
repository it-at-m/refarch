package de.muenchen.refarch.integration.cosys.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class NoSecurityConfiguration {

    @Bean
    public SecurityFilterChain mainSecurityFilterChain(final HttpSecurity http) throws Exception {
        http
                .headers(httpSecurityHeadersConfigurer -> httpSecurityHeadersConfigurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .authorizeHttpRequests(authorizeRequest -> {
                    authorizeRequest.anyRequest().permitAll();
                })
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

}
