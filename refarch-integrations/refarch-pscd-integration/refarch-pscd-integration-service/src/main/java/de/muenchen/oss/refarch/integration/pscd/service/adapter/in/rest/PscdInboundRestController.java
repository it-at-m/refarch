package de.muenchen.oss.refarch.integration.pscd.service.adapter.in.rest;

import de.muenchen.oss.refarch.integration.pscd.application.port.in.SubmitPscdBatchInPort;
import de.muenchen.oss.refarch.integration.pscd.domain.exception.PscdValidationException;
import de.muenchen.oss.refarch.integration.pscd.domain.model.PscdSatzarten;
import de.muenchen.oss.refarch.integration.pscd.inbound.model.PscdBatch;
import de.muenchen.oss.refarch.integration.pscd.service.adapter.in.PscdBatchLog;
import de.muenchen.oss.refarch.integration.pscd.service.adapter.in.PscdInboundCanonicalMapper;
import de.muenchen.oss.refarch.integration.pscd.service.adapter.in.PscdInboundChannel;
import de.muenchen.oss.refarch.integration.pscd.service.adapter.out.notification.PscdFailureNotifier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST inbound adapter for the refarch-owned canonical contract.
 *
 * <p>
 * Accepts the same canonical {@link PscdBatch} as the SOAP edge, here as JSON, maps it onto the
 * domain and hands it to the single inbound port. Returns {@code 202 Accepted}; malformed JSON is
 * rejected by the framework as {@code 400} before it reaches here. Enabled by
 * {@code refarch.pscd.inbound.rest.enabled} (default on).
 * </p>
 *
 * <p>
 * Fire-and-forget: the port call below is synchronous
 * all the way to the PSCD SOAP endpoint, so the caller waits for that leg and sees a {@code 5xx} if
 * it fails. What the caller never learns is what PSCD made of the batch afterwards; that comes back
 * on no channel, which is why the inbound log line and the failure mail exist.
 * </p>
 */
@RestController
@RequestMapping("/api/pscd")
@ConditionalOnProperty(name = "refarch.pscd.inbound.rest.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "PSCD Inbound", description = "Submission of canonical PSCD \"Satzarten\" batches over REST")
public class PscdInboundRestController {

    private static final PscdInboundChannel CHANNEL = PscdInboundChannel.REST;

    private final SubmitPscdBatchInPort submitPscdBatchInPort;
    private final PscdFailureNotifier failureNotifier;

    @Operation(
            summary = "Submit a canonical PSCD batch",
            description = "Accepts a canonical PSCD batch as JSON, maps it onto the domain and hands it to the "
                    + "inbound port. Delivery to PSCD happens within the request: the call blocks until the "
                    + "downstream SOAP endpoint has accepted the batch, so size the client timeout for that leg "
                    + "and not for this one. 202 Accepted means the batch was delivered, not merely queued; "
                    + "a failed delivery surfaces as 5xx. Nothing about what PSCD then did with the batch is "
                    + "reported back; that is the fire-and-forget part."
    )
    @ApiResponses(
        {
                @ApiResponse(responseCode = "202", description = "Batch delivered to the PSCD endpoint", content = @Content),
                @ApiResponse(
                        responseCode = "400",
                        description = "Structurally invalid batch (e.g. missing the mandatory filename or record)",
                        content = @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class)
                        )
                )
        }
    )
    @PostMapping(path = "/batches", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> submitBatch(@RequestBody final PscdBatch batch) {
        final PscdSatzarten domainBatch = toDomain(batch);
        // Logged on receipt rather than after delivery: the call is fire-and-forget, so this line must
        // exist even when the downstream delivery below fails.
        PscdBatchLog.logAccepted(log, CHANNEL.label(), domainBatch);
        submit(domainBatch);
        return ResponseEntity.accepted().build();
    }

    /**
     * Map the canonical batch onto the domain, logging a rejection here rather than in
     * {@link #handleValidation}: this is the only place where the offending batch's filename is still
     * available. The notification goes out from here for the same reason.
     */
    private PscdSatzarten toDomain(final PscdBatch batch) {
        try {
            return PscdInboundCanonicalMapper.toDomain(batch);
        } catch (final PscdValidationException e) {
            final String filename = batch == null ? null : batch.getFilename();
            PscdBatchLog.logRejected(log, CHANNEL.label(), filename, e);
            this.failureNotifier.notifyFailure(CHANNEL, filename, null, e);
            throw e;
        }
    }

    /**
     * Hand the batch to the core, notifying on the way out. A delivery failure is reported to the
     * caller as a {@code 5xx} either way, but it is also the unattended half of this channel: nobody
     * on this side sees that PSCD refused the batch unless the mail says so.
     */
    private void submit(final PscdSatzarten batch) {
        try {
            this.submitPscdBatchInPort.submit(batch);
        } catch (final RuntimeException e) {
            this.failureNotifier.notifyFailure(CHANNEL, batch.getFilename(), null, e);
            throw e;
        }
    }

    /**
     * A structurally invalid batch (e.g. missing the mandatory record) is a caller error →
     * {@code 400}. A downstream delivery failure ({@code PscdProcessingException}) is deliberately not
     * handled here and surfaces as a {@code 5xx}.
     *
     * <p>
     * Response mapping only; the rejection is logged in {@link #toDomain}, where the batch itself is
     * still in scope.
     * </p>
     */
    @ExceptionHandler(PscdValidationException.class)
    public ProblemDetail handleValidation(final PscdValidationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
