package de.muenchen.refarch.integration.dms.fabasoft.mock.service.cases;

import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.UpdateInternalGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.UpdateInternalGIResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import de.muenchen.refarch.integration.dms.fabasoft.mock.WiremockWsdlUtility;
import org.springframework.stereotype.Component;

@Component
public class UpdateInternalDocumentCase implements MockCase {

    @Override
    public void initCase(final WireMockServer server) {

        final UpdateInternalGIResponse updateInternalGIResponse = new UpdateInternalGIResponse();
        updateInternalGIResponse.setObjid("1234567890");

        WiremockWsdlUtility.stubOperation(
                server,
                "UpdateInternalGI",
                UpdateInternalGI.class, (u) -> true,
                updateInternalGIResponse);

    }

}
