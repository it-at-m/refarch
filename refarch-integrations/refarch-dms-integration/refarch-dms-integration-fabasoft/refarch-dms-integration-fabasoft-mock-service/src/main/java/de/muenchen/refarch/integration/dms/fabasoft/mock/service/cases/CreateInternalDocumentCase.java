package de.muenchen.refarch.integration.dms.fabasoft.mock.service.cases;

import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateInternalGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateInternalGIResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import de.muenchen.refarch.integration.dms.fabasoft.mock.WiremockWsdlUtility;
import org.springframework.stereotype.Component;

@Component
public class CreateInternalDocumentCase implements MockCase {

    @Override
    public void initCase(final WireMockServer server) {

        final CreateInternalGIResponse createInternalGIResponse = new CreateInternalGIResponse();
        createInternalGIResponse.setObjid("1234567890");

        WiremockWsdlUtility.stubOperation(
                server,
                "CreateInternalGI",
                CreateInternalGI.class, (u) -> true,
                createInternalGIResponse);

    }

}
