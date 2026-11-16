package de.muenchen.oss.refarch.integration.pscd.service.adapter.in.soap;

import de.muenchen.oss.refarch.integration.pscd.application.port.in.SubmitPscdBatchInPort;
import de.muenchen.oss.refarch.integration.pscd.domain.exception.PscdValidationException;
import de.muenchen.oss.refarch.integration.pscd.domain.model.PscdSatzarten;
import de.muenchen.oss.refarch.integration.pscd.inbound.model.PscdBatch;
import de.muenchen.oss.refarch.integration.pscd.service.adapter.in.PscdBatchLog;
import de.muenchen.oss.refarch.integration.pscd.service.adapter.in.PscdInboundCanonicalMapper;
import de.muenchen.oss.refarch.integration.pscd.service.adapter.in.PscdInboundChannel;
import de.muenchen.oss.refarch.integration.pscd.service.adapter.out.notification.PscdFailureNotifier;
import jakarta.xml.bind.JAXBElement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;

/**
 * SOAP inbound endpoint for the refarch-owned canonical contract.
 *
 * <p>
 * Spring WS dispatches on the payload root element rather than on a URL, so every request arriving
 * under the {@code MessageDispatcherServlet} mapping whose body root is {@code pscdBatch} reaches
 * {@link #submitBatch}. It is the SOAP
 * counterpart of {@code PscdInboundRestController}. One-way, fire-and-forget: the method returns
 * nothing, so no response payload is produced.
 * </p>
 *
 * <p>
 * The enablement guard sits on this class rather than on {@link PscdSoapInboundConfiguration},
 * because {@code @Endpoint} is itself a {@code @Component}: the starter's component scan would
 * otherwise register the endpoint even with {@code refarch.pscd.inbound.soap.enabled=false}. Same
 * arrangement as {@code PscdInboundRestController}.
 * </p>
 */
@Endpoint
@ConditionalOnProperty(name = "refarch.pscd.inbound.soap.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class PscdSoapInboundEndpoint {

    /** Target namespace of the canonical contract; shared by the WSDL and the schema. */
    private static final String NAMESPACE = "https://refarch.muenchen.de/pscd/inbound/v1";

    private static final PscdInboundChannel CHANNEL = PscdInboundChannel.SOAP;

    private final SubmitPscdBatchInPort submitPscdBatchInPort;
    private final PscdFailureNotifier failureNotifier;

    /**
     * The canonical schema declares {@code pscdBatch} as a global element over the separately named
     * {@code PscdBatch} complex type, so XJC binds it through the {@code ObjectFactory} rather than
     * adding {@code @XmlRootElement}, hence the {@link JAXBElement} wrapper on the payload.
     *
     * @param request the canonical batch as sent by the caller
     */
    @PayloadRoot(namespace = NAMESPACE, localPart = "pscdBatch")
    public void submitBatch(@RequestPayload final JAXBElement<PscdBatch> request) {
        final PscdBatch pscdBatch = request == null ? null : request.getValue();
        final PscdSatzarten domainBatch = toDomain(pscdBatch);
        // Logged on receipt rather than after delivery: the call is one-way, so this line must exist
        // even when the downstream delivery below fails.
        PscdBatchLog.logAccepted(log, CHANNEL.label(), domainBatch);
        submit(domainBatch);
    }

    /**
     * Map the canonical batch onto the domain, logging a rejection on the way out. Spring WS turns the
     * rethrown exception into a fault but logs nothing about it, and this is the only place where the
     * offending batch's filename is still available, the counterpart of the REST edge's
     * {@code @ExceptionHandler}. The notification goes out from here for the same reason.
     */
    private PscdSatzarten toDomain(final PscdBatch pscdBatch) {
        try {
            return PscdInboundCanonicalMapper.toDomain(pscdBatch);
        } catch (final PscdValidationException e) {
            final String filename = pscdBatch == null ? null : pscdBatch.getFilename();
            PscdBatchLog.logRejected(log, CHANNEL.label(), filename, e);
            // No location: this channel does not keep the payload, the caller still holds it.
            this.failureNotifier.notifyFailure(CHANNEL, filename, null, e);
            throw e;
        }
    }

    /**
     * Hand the batch to the core, notifying on the way out. The caller gets a fault either way, but a
     * one-way sender is rarely watching for one, so the mail is what makes a refused batch visible on
     * this side.
     */
    private void submit(final PscdSatzarten batch) {
        try {
            this.submitPscdBatchInPort.submit(batch);
        } catch (final RuntimeException e) {
            this.failureNotifier.notifyFailure(CHANNEL, batch.getFilename(), null, e);
            throw e;
        }
    }
}
