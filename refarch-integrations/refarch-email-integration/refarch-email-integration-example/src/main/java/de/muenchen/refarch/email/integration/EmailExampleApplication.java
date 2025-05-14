package de.muenchen.refarch.email.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@RequiredArgsConstructor
public class EmailExampleApplication {
    private final ExampleMailService exampleMailService;
    private final ApplicationContext context;

    public static void main(final String[] args) {
        SpringApplication.run(EmailExampleApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void sendTestMail() {
        this.exampleMailService.testSendMail();
        this.exampleMailService.testSendMailTemplate();
        SpringApplication.exit(context);
    }
}
