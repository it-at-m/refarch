package de.muenchen.refarch.integration.dms.example;

import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@RequiredArgsConstructor
@EnableConfigurationProperties(DmsExampleProperties.class)
public class DmsExampleApplication {
    private final ExampleDmsService exampleDmsService;
    private final ApplicationContext context;

    public static void main(final String[] args) {
        SpringApplication.run(DmsExampleApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void testDms() throws IOException, DmsException {
        this.exampleDmsService.testCreateDocument();
        SpringApplication.exit(context);
    }
}
