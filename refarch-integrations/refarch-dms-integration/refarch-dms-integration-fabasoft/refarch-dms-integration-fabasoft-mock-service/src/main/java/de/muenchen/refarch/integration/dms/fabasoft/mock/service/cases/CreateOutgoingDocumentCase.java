package de.muenchen.refarch.integration.dms.fabasoft.mock.service.cases;

import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateOutgoingGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateOutgoingGIResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import de.muenchen.refarch.integration.dms.fabasoft.mock.WiremockWsdlUtility;
import lombok.val;
import org.springframework.stereotype.Component;

@Component
public class CreateOutgoingDocumentCase implements MockCase {

    @Override
    public void initCase(WireMockServer server) {

        val createOutgoingGIResponse = new CreateOutgoingGIResponse();
        createOutgoingGIResponse.setObjid("1234567890");

        WiremockWsdlUtility.stubOperation(
                server,
                "CreateOutgoingGI",
                CreateOutgoingGI.class, (u) -> true,
                createOutgoingGIResponse);

    }

}
