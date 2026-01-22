package de.muenchen.refarch.integration.s3.example;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@ConfigurationPropertiesScan
@RequiredArgsConstructor
public class S3ExampleApplication {
    private final ApplicationContext context;
    private final S3ExampleService exampleService;

    public static void main(final String[] args) {
        SpringApplication.run(S3ExampleApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void sendTestMail() throws Exception {
        this.exampleService.testS3();
        System.exit(SpringApplication.exit(context));
    }
}
