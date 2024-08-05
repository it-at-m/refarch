package de.muenchen.oss.digiwf.s3.integration;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.digiwf.s3.integration.application.port.in.CreatePresignedUrlsInPort;
import de.muenchen.oss.digiwf.s3.integration.configuration.S3IntegrationAutoConfiguration;
import de.muenchen.oss.digiwf.s3.integration.domain.model.CreatePresignedUrlEvent;
import de.muenchen.refarch.spring.security.security.authentication.UserAuthenticationProvider;
import de.muenchen.refarch.spring.security.security.autoconfiguration.SpringSecurityAutoConfiguration;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = { S3IntegrationApplication.class, S3IntegrationAutoConfiguration.class }
)
@ActiveProfiles({ "itest", "local" })
@EnableAutoConfiguration(
        exclude = {
                SpringSecurityAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
@EmbeddedKafka(
        partitions = 1,
        topics = {
                "${spring.cloud.stream.bindings.functionRouter-in-0.destination}",
                "${spring.cloud.stream.bindings.sendMessage-out-0.destination}"
        }
)
public class ServiceIntegrationITest {

    @MockBean
    private UserAuthenticationProvider provider;

    @Autowired(required = false)
    private CreatePresignedUrlsInPort port;

    @Autowired(required = false)
    private Consumer<Message<CreatePresignedUrlEvent>> createPresignedUrl;

    @Test
    void starts_service() {
        assertThat(port).isNotNull();
        assertThat(createPresignedUrl).isNotNull();
    }
}
