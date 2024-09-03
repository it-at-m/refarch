package de.muenchen.refarch.integration.dms.fabasoft.mock.service.cases;

import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.UpdateIncomingGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.UpdateIncomingGIResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import de.muenchen.refarch.integration.dms.fabasoft.mock.WiremockWsdlUtility;
import lombok.val;
import org.springframework.stereotype.Component;

@Component
public class UpdateIncomingDocumentCase implements MockCase {

    @Override
    public void initCase(WireMockServer server) {

        val updateIncomingGIResponse = new UpdateIncomingGIResponse();
        updateIncomingGIResponse.setObjid("1234567890");

        WiremockWsdlUtility.stubOperation(
                server,
                "UpdateIncomingGI",
                UpdateIncomingGI.class, (u) -> true,
                updateIncomingGIResponse);

    }

}
