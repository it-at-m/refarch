package de.muenchen.refarch.integration.dms.fabasoft.mock;

import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.LHMBAI151700GIWSD;
import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.LHMBAI151700GIWSDSoap;

import jakarta.xml.ws.BindingProvider;

/**
 * Configuration of the Webservice Client.
 */
public final class FabasoftClienFactory {
    private FabasoftClienFactory() {
    }

    public static LHMBAI151700GIWSDSoap dmsWsClient(final String address) {
        final LHMBAI151700GIWSD service = new LHMBAI151700GIWSD();
        final LHMBAI151700GIWSDSoap soapClient = service.getLHMBAI151700GIWSDSoap();
        ((BindingProvider) soapClient).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, address);
        return soapClient;
    }

}
