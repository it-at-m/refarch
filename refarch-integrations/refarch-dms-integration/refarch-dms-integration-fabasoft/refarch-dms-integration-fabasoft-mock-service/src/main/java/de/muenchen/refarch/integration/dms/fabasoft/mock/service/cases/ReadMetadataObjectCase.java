package de.muenchen.refarch.integration.dms.fabasoft.mock.service.cases;

import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.ReadMetadataObjectGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.ReadMetadataObjectGIResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import de.muenchen.refarch.integration.dms.fabasoft.mock.WiremockWsdlUtility;
import lombok.val;

import org.springframework.stereotype.Component;

@Component
public class ReadMetadataObjectCase implements MockCase {

    @Override
    public void initCase(WireMockServer server) {

        val response = new ReadMetadataObjectGIResponse();
        response.setStatus(0);
        response.setObjclass("Vorgang");
        response.setObjname("name");

        WiremockWsdlUtility.stubOperation(
                server,
                "ReadMetadataObjectGI",
                ReadMetadataObjectGI.class, (u) -> true,
                response);

    }

}
