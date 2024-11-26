package de.muenchen.refarch.integration.dms.fabasoft.mock.service.cases;

import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateProcedureGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.DepositObjectGIResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import de.muenchen.refarch.integration.dms.fabasoft.mock.WiremockWsdlUtility;
import org.springframework.stereotype.Component;

@Component
public class DepositObjectCase implements MockCase {

    @Override
    public void initCase(final WireMockServer server) {

        final DepositObjectGIResponse depositObjectGIResponse = new DepositObjectGIResponse();
        depositObjectGIResponse.setObjid("1234567890");

        WiremockWsdlUtility.stubOperation(
                server,
                "DepositObjectGI",
                CreateProcedureGI.class, (u) -> true,
                depositObjectGIResponse);

    }

}
