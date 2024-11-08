package de.muenchen.refarch.integration.dms.fabasoft.mock.service.cases;

import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.LHMBAI151700GIMetadataType;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.ReadContentObjectMetaDataGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.ReadContentObjectMetaDataGIResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import de.muenchen.refarch.integration.dms.fabasoft.mock.WiremockWsdlUtility;
import org.springframework.stereotype.Component;

@Component
public class ReadContentObjectMetaDataCase implements MockCase {

    @Override
    public void initCase(final WireMockServer server) {

        final LHMBAI151700GIMetadataType content = new LHMBAI151700GIMetadataType();
        content.setLHMBAI151700Filename("name");
        content.setLHMBAI151700Objclass("pdf");

        final ReadContentObjectMetaDataGIResponse response = new ReadContentObjectMetaDataGIResponse();
        response.setStatus(0);
        response.setGimetadatatype(content);

        WiremockWsdlUtility.stubOperation(
                server,
                "ReadContentObjectMetaDataGI",
                ReadContentObjectMetaDataGI.class, (u) -> true,
                response);

    }

}
