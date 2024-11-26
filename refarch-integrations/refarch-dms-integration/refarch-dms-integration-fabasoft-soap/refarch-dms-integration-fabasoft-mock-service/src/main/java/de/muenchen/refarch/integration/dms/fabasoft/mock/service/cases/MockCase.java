package de.muenchen.refarch.integration.dms.fabasoft.mock.service.cases;

import com.github.tomakehurst.wiremock.WireMockServer;

public interface MockCase {
    void initCase(WireMockServer server);
}
