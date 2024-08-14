package de.muenchen.refarch.email.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class EmailRestExampleApplication {
    @Autowired
    TestService testService;

    public static void main(final String[] args) {
        SpringApplication.run(EmailRestExampleApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    void sendTestMail() {
        this.testService.testSendMail();
        System.exit(0);
    }
}
