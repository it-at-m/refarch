package de.muenchen.oss.refarch.integration.pscd.client;

import de.lhm.pi.pscd.rahmenschnittstelle.DTSOAPSatzarten;
import de.lhm.pi.pscd.rahmenschnittstelle.SISOAPSatzartenASOB;
import java.time.Duration;
import java.util.Objects;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

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
     * A client that calls the endpoint without credentials, with CXF's default timeouts.
     *
     * @param endpointUrl the SOAP endpoint address to call (configured via
     *            {@code refarch.pscd.client.url})
     */
    public PscdSoapClient(final String endpointUrl) {
        this.port = createPort(endpointUrl, null, null);
    }

    /**
     * A client that calls the endpoint with credentials and the given timeouts.
     *
     * @param endpointUrl the SOAP endpoint address to call (configured via
     *            {@code refarch.pscd.client.url})
     * @param username HTTP Basic username, or {@code null}/blank to send no credentials
     * @param password password for {@code username}; ignored when no username is given
     * @param connectionTimeout how long to wait for the connection to the endpoint; at least one
     *            millisecond
     * @param receiveTimeout how long to wait for the endpoint's acknowledgement once the message is
     *            sent; at least one millisecond
     */
    public PscdSoapClient(final String endpointUrl, final String username, final String password,
            final Duration connectionTimeout, final Duration receiveTimeout) {
        final long connectionTimeoutMillis = requireAtLeastOneMillisecond(connectionTimeout, "connectionTimeout");
        final long receiveTimeoutMillis = requireAtLeastOneMillisecond(receiveTimeout, "receiveTimeout");
        this.port = createPort(endpointUrl, username, password);
        final HTTPClientPolicy policy = ((HTTPConduit) ClientProxy.getClient(this.port).getConduit()).getClient();
        policy.setConnectionTimeout(connectionTimeoutMillis);
        policy.setReceiveTimeout(receiveTimeoutMillis);
    }

    private static SISOAPSatzartenASOB createPort(final String endpointUrl, final String username, final String password) {
        final JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(SISOAPSatzartenASOB.class);
        factory.setAddress(endpointUrl);
        if (username != null && !username.isBlank()) {
            factory.setUsername(username);
            factory.setPassword(password);
        }
        return (SISOAPSatzartenASOB) factory.create();
    }

    private static long requireAtLeastOneMillisecond(final Duration timeout, final String name) {
        Objects.requireNonNull(timeout, name + " must not be null");
        // CXF reads a timeout of 0 as "wait forever", and a sub-millisecond Duration truncates to 0.
        if (timeout.toMillis() < 1) {
            throw new IllegalArgumentException(name + " must be at least 1ms, was " + timeout);
        }
        return timeout.toMillis();
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
