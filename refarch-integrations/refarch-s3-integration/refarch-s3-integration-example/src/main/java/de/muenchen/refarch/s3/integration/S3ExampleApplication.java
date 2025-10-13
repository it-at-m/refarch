package de.muenchen.refarch.s3.integration;

import de.muenchen.refarch.s3.integration.java.JavaExampleService;
import de.muenchen.refarch.s3.integration.rest.RestExampleService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@ConfigurationPropertiesScan
@RequiredArgsConstructor
@Slf4j
public class S3ExampleApplication {
    private final ApplicationContext context;
    private final JavaExampleService javaExampleService;
    private final Optional<RestExampleService> restExampleService;

    public static void main(final String[] args) {
        SpringApplication.run(S3ExampleApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void sendTestMail() throws Exception {
        // Java
        log.info("Testing S3 via Java");
        this.javaExampleService.testS3();
        // REST
        if (restExampleService.isPresent()) {
            log.info("Testing S3 via Rest");
            restExampleService.get().testS3();
        } else {
            log.warn("Testing via Rest disabled - Enabled with profile 'rest'");
        }
        log.info("Finished testing");
        SpringApplication.exit(context);
    }
}
