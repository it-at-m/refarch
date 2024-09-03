package de.muenchen.refarch.integration.dms.fabasoft.mock.service;

import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateProcedureGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CreateProcedureGIResponse;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.LHMBAI151700GIWSDSoap;
import de.muenchen.refarch.integration.dms.fabasoft.mock.FabasoftClienFactory;
import de.muenchen.refarch.integration.dms.fabasoft.mock.service.FabasoftMockApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = FabasoftMockApplication.class, properties = "mock.port=9070", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("itest")
public class ExampleTest {

    @Value("${mock.port:9070}")
    private int port;

    private LHMBAI151700GIWSDSoap soapClient;

    @BeforeEach
    public void setUp() {
        this.soapClient = FabasoftClienFactory.dmsWsClient("http://localhost:" + port + "/");
    }

    @Test
    public void execute_createProcedure_request() {
        final CreateProcedureGI request = new CreateProcedureGI();
        request.setUserlogin("user");
        request.setReferrednumber("fileCOO");
        request.setBusinessapp("businessapp");
        request.setShortname("new procedure");
        request.setFilesubj("new procedure");
        request.setFiletype("Elektronisch");

        final CreateProcedureGIResponse response = this.soapClient.createProcedureGI(request);

    }

}
