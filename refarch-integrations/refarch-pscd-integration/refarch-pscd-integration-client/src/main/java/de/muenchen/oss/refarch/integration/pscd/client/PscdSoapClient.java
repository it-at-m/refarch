package de.muenchen.oss.refarch.integration.pscd.client;

import de.lhm.pi.pscd.rahmenschnittstelle.DTSOAPSatzarten;
import de.lhm.pi.pscd.rahmenschnittstelle.SISOAPSatzartenASOB;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

/**
 * Thin SOAP client that calls a configurable PSCD endpoint.
 *
 * <p>
 * The endpoint may be protected with HTTP Basic ({@code refarch.pscd.client.username} and
 * {@code .password}). Credentials are optional.
 * </p>
 */
public class PscdSoapClient {

    private final SISOAPSatzartenASOB port;

    /**
     * A client that calls the endpoint without credentials.
     *
     * @param endpointUrl the SOAP endpoint address to call (configured via
     *            {@code refarch.pscd.client.url})
     */
    public PscdSoapClient(final String endpointUrl) {
        this(endpointUrl, null, null);
    }

    /**
     * A client that calls the endpoint with credentials.
     *
     * @param endpointUrl the SOAP endpoint address to call (configured via
     *            {@code refarch.pscd.client.url})
     * @param username HTTP Basic username, or {@code null}/blank to send no credentials
     * @param password password for {@code username}; ignored when no username is given
     */
    public PscdSoapClient(final String endpointUrl, final String username, final String password) {
        final JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(SISOAPSatzartenASOB.class);
        factory.setAddress(endpointUrl);
        if (username != null && !username.isBlank()) {
            factory.setUsername(username);
            factory.setPassword(password);
        }
        this.port = (SISOAPSatzartenASOB) factory.create();
    }

    /**
     * Send a batch of PSCD records to the endpoint. One-way: the call completes once the message has
     * been dispatched; the service returns no response.
     *
     * @param satzarten the SOAP message to deliver
     */
    public void send(final DTSOAPSatzarten satzarten) {
        this.port.siSOAPSatzartenASOB(satzarten);
    }
}
