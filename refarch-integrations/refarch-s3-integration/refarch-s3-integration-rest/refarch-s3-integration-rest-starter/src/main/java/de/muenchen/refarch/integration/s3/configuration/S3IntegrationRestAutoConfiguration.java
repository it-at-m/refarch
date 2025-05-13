package de.muenchen.refarch.integration.s3.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@ComponentScan(basePackages = "de.muenchen.refarch.integration.s3")
public class S3IntegrationRestAutoConfiguration {
}
