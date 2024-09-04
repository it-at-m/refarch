package de.muenchen.refarch.email.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@RequiredArgsConstructor
public class EmailJavaExampleApplication {
    private final TestService testService;

    public static void main(final String[] args) {
        SpringApplication.run(EmailJavaExampleApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    void sendTestMail() {
        this.testService.testSendMail();
        System.exit(0);
    }
}
