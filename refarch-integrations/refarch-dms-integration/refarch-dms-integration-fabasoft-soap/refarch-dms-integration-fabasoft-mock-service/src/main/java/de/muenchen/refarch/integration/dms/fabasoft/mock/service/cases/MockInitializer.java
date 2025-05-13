package de.muenchen.refarch.integration.dms.fabasoft.mock.service.cases;

import com.github.tomakehurst.wiremock.WireMockServer;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MockInitializer {

    private final List<MockCase> mockCases;

    @Value("${mock.port:9070}")
    private int port;

    @PostConstruct
    public void init() {
        final WireMockServer server = new WireMockServer(port);
        server.start();
        mockCases.forEach(mock -> mock.initCase(server));
    }
}
