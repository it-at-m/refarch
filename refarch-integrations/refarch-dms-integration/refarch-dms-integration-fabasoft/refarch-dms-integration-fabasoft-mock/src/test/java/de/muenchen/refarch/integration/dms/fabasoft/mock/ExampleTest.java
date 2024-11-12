package de.muenchen.refarch.integration.dms.fabasoft.mock;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateProcedureGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateProcedureGIResponse;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.LHMBAI151700GIWSDSoap;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@WireMockTest()
class ExampleTest {

    private LHMBAI151700GIWSDSoap soapClient;

    @BeforeEach
    public void setUp(final WireMockRuntimeInfo wmRuntimeInfo) {
        soapClient = FabasoftClientFactory.dmsWsClient("http://localhost:" + wmRuntimeInfo.getHttpPort() + "/");
    }

    @Test
    void executeCreateProcedureRequest() {
        WiremockWsdlUtility.stubOperation(
                "CreateProcedureGI",
                CreateProcedureGI.class, (u) -> "new procedure".equals(u.getShortname()),
                new CreateProcedureGIResponse());

        final CreateProcedureGI request = new CreateProcedureGI();
        request.setUserlogin("user");
        request.setReferrednumber("fileCOO");
        request.setBusinessapp("businessapp");
        request.setShortname("new procedure");
        request.setFilesubj("new procedure");
        request.setFiletype("Elektronisch");

        final CreateProcedureGIResponse response = this.soapClient.createProcedureGI(request);

        assertNotNull(response);
    }

}
