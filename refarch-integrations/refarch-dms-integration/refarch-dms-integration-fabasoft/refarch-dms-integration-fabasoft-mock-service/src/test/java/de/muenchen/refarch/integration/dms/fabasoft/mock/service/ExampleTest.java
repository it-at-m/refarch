package de.muenchen.refarch.integration.dms.fabasoft.mock.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateProcedureGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateProcedureGIResponse;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.LHMBAI151700GIWSDSoap;
import de.muenchen.refarch.integration.dms.fabasoft.mock.FabasoftClienFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = FabasoftMockApplication.class, properties = "mock.port=9070", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("itest")
class ExampleTest {

    @Value("${mock.port:9070}")
    private int port;

    private LHMBAI151700GIWSDSoap soapClient;

    @BeforeEach
    void setUp() {
        this.soapClient = FabasoftClienFactory.dmsWsClient("http://localhost:" + port + "/");
    }

    @Test
    @SuppressWarnings({ "PMD.JUnitTestsShouldIncludeAssert", "PMD.UnusedLocalVariable" })
    void executeCreateProcedureRequest() {
        final CreateProcedureGI request = new CreateProcedureGI();
        request.setUserlogin("user");
        request.setReferrednumber("fileCOO");
        request.setBusinessapp("businessapp");
        request.setShortname("new procedure");
        request.setFilesubj("new procedure");
        request.setFiletype("Elektronisch");

        final CreateProcedureGIResponse response = this.soapClient.createProcedureGI(request);
        assertEquals("1234567890", response.getObjid());
    }

}
