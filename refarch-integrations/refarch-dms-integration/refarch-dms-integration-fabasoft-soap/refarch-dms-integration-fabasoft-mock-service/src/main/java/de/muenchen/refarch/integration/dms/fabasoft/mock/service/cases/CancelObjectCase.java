package de.muenchen.refarch.integration.dms.fabasoft.mock.service.cases;

import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CancelObjectGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CancelObjectGIResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import de.muenchen.refarch.integration.dms.fabasoft.mock.WiremockWsdlUtility;
import org.springframework.stereotype.Component;

@Component
public class CancelObjectCase implements MockCase {

    @Override
    public void initCase(final WireMockServer server) {

        final CancelObjectGIResponse cancelObjectGIResponse = new CancelObjectGIResponse();
        cancelObjectGIResponse.setStatus(0);

        WiremockWsdlUtility.stubOperation(
                server,
                "CancelObjectGI",
                CancelObjectGI.class, (u) -> true,
                cancelObjectGIResponse);

    }

}
