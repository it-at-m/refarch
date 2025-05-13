package de.muenchen.refarch.integration.dms.fabasoft.mock.service.cases;

import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.CancelObjectGI;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.LHMBAI151700GIAttachmentType;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.ReadContentObjectGIResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import de.muenchen.refarch.integration.dms.fabasoft.mock.WiremockWsdlUtility;
import java.io.IOException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class ReadContentCase implements MockCase {

    @Override
    public void initCase(final WireMockServer server) {

        final LHMBAI151700GIAttachmentType content = new LHMBAI151700GIAttachmentType();
        content.setLHMBAI151700Filename("test");
        content.setLHMBAI151700Fileextension("pdf");

        try {
            final Resource resource = new ClassPathResource("data/test.pdf");
            final byte[] data = resource.getInputStream().readAllBytes();
            content.setLHMBAI151700Filecontent(data);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        final ReadContentObjectGIResponse response = new ReadContentObjectGIResponse();
        response.setStatus(0);
        response.setGiattachmenttype(content);

        WiremockWsdlUtility.stubOperation(
                server,
                "ReadContentObjectGI",
                CancelObjectGI.class, (u) -> true,
                response);

    }

}
