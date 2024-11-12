package de.muenchen.refarch.integration.dms.fabasoft.mock.service.cases;

import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.ArrayOfLHMBAI151700GIObjectType;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.LHMBAI151700GIObjectType;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.SearchObjNameGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.SearchObjNameGIResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import de.muenchen.refarch.integration.dms.fabasoft.mock.WiremockWsdlUtility;
import org.springframework.stereotype.Component;

@Component
public class SearchFileCase implements MockCase {

    @Override
    public void initCase(final WireMockServer server) {

        final LHMBAI151700GIObjectType file = new LHMBAI151700GIObjectType();
        file.setLHMBAI151700Objaddress("fileCoo");
        file.setLHMBAI151700Objname("fileName");

        final ArrayOfLHMBAI151700GIObjectType array = new ArrayOfLHMBAI151700GIObjectType();
        array.getLHMBAI151700GIObjectType().add(file);

        final SearchObjNameGIResponse response = new SearchObjNameGIResponse();
        response.setStatus(0);
        response.setGiobjecttype(array);

        WiremockWsdlUtility.stubOperation(
                server,
                "SearchObjNameGI",
                SearchObjNameGI.class, (u) -> u.getObjclass().equals(DMSObjectClass.Sachakte.getName()),
                response);
    }

}
