package de.muenchen.refarch.integration.dms.fabasoft.mock.service.cases;

import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateFileGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateFileGIResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import de.muenchen.refarch.integration.dms.fabasoft.mock.WiremockWsdlUtility;
import lombok.val;
import org.springframework.stereotype.Component;

@Component
public class CreateFileCase implements MockCase {

    @Override
    public void initCase(WireMockServer server) {

        val createFileGIResponse = new CreateFileGIResponse();
        createFileGIResponse.setObjid("1234567890");

        WiremockWsdlUtility.stubOperation(
                server,
                "CreateFileGI",
                CreateFileGI.class, (u) -> true,
                createFileGIResponse);

    }

}
