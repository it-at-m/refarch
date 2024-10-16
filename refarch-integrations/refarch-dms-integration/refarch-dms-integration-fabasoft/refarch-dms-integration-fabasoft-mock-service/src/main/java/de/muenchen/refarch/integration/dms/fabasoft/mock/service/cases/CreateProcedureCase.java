package de.muenchen.refarch.integration.dms.fabasoft.mock.service.cases;

import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateProcedureGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateProcedureGIResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import de.muenchen.refarch.integration.dms.fabasoft.mock.WiremockWsdlUtility;
import org.springframework.stereotype.Component;

@Component
public class CreateProcedureCase implements MockCase {

    @Override
    public void initCase(final WireMockServer server) {

        final CreateProcedureGIResponse createProcedureGIResponse = new CreateProcedureGIResponse();
        createProcedureGIResponse.setObjid("1234567890");

        WiremockWsdlUtility.stubOperation(
                server,
                "CreateProcedureGI",
                CreateProcedureGI.class, (u) -> true,
                createProcedureGIResponse);

    }

}
