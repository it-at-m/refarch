package de.muenchen.oss.digiwf.dms.integration.fabasoft.mock.service.cases;

import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateIncomingGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateIncomingGIResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import de.muenchen.oss.digiwf.integration.e2e.test.wsdl.DigiwfWiremockWsdlUtility;
import lombok.val;
import org.springframework.stereotype.Component;

@Component
public class CreateIncomingDocumentCase implements MockCase {

    @Override
    public void initCase(WireMockServer server) {

        val createIncomingGIResponse = new CreateIncomingGIResponse();
        createIncomingGIResponse.setObjid("1234567890");

        DigiwfWiremockWsdlUtility.stubOperation(
                server,
                "CreateIncomingGI",
                CreateIncomingGI.class, (u) -> true,
                createIncomingGIResponse);

    }

}
