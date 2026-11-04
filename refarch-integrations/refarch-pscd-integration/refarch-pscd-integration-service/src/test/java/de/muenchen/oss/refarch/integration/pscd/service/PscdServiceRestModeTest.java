package de.muenchen.oss.refarch.integration.pscd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.muenchen.oss.refarch.integration.email.application.port.out.MailOutPort;
import de.muenchen.oss.refarch.integration.email.domain.model.TextMail;
import de.muenchen.oss.refarch.integration.pscd.application.port.out.PscdOutPort;
import de.muenchen.oss.refarch.integration.pscd.domain.model.PscdSatzarten;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// Isolate the REST channel: only rest on, the other two off for isolation. The OpenAPI assertion below
// doubles as the guard that the SOAP edge's schema-first codegen still produces the shared canonical
// model this controller binds.
@SpringBootTest(
        properties = {
                "refarch.pscd.inbound.rest.enabled=true",
                "refarch.pscd.inbound.soap.enabled=false",
                "refarch.pscd.inbound.file.enabled=false",
                // The HTTP channels always require HTTP Basic and have no default account.
                "refarch.pscd.inbound.security.username=pscd-sender",
                "refarch.pscd.inbound.security.password=s3cret",
                // Notification on for this channel only, so the rejection below must mail and nothing else may.
                "refarch.pscd.notification.rest.enabled=true",
                "refarch.pscd.notification.to=ops@example.org"
        }
)
@AutoConfigureMockMvc
class PscdServiceRestModeTest {

    /** The inbound REST endpoint under test. */
    private static final String BATCHES = "/api/pscd/batches";

    /**
     * The HTTP channels always require HTTP Basic, so every submission here carries the account this
     * context configures. {@link PscdServiceInboundSecurityTest} covers the rejection paths.
     */
    private static final String BASIC_AUTH = "Basic "
            + Base64.getEncoder().encodeToString("pscd-sender:s3cret".getBytes(StandardCharsets.UTF_8));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PscdOutPort pscdOutPort;

    @MockitoBean
    private MailOutPort mailOutPort;

    @Test
    void acceptsACanonicalJsonBatchAndDeliversItToTheOutPort() throws Exception {
        final String json = """
                {
                  "filename": "rest-batch.json",
                  "satzart010": { "satzart": "010", "abstimmsumme": "12345", "vorzeichen": "+" },
                  "satzart100": [ { "satzart": "100", "psobkey": "KEY1", "partner": "PARTNER1" } ],
                  "satzart200": [
                    { "satzart": "200", "psobkey": "KEY2", "einnahmeart": "EA", "betrw": "100,00",
                      "faedn": "20260131", "bldat": "20260101", "xblnr": "INV-1", "fvBelnr": "FV-1" },
                    { "satzart": "200", "psobkey": "KEY3", "einnahmeart": "EA", "betrw": "50,00",
                      "faedn": "20260228", "bldat": "20260101", "xblnr": "INV-2", "fvBelnr": "FV-2" }
                  ]
                }
                """;

        this.mockMvc.perform(post(BATCHES).header(HttpHeaders.AUTHORIZATION, BASIC_AUTH).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isAccepted());

        final ArgumentCaptor<PscdSatzarten> captor = ArgumentCaptor.forClass(PscdSatzarten.class);
        verify(this.pscdOutPort, times(1)).send(captor.capture());
        final PscdSatzarten batch = captor.getValue();
        assertThat(batch.getFilename()).isEqualTo("rest-batch.json");
        assertThat(batch.getSatzart010().getAbstimmsumme()).isEqualTo("12345");
        assertThat(batch.getSatzart100()).singleElement()
                .satisfies(record -> assertThat(record.getPartner()).isEqualTo("PARTNER1"));
        assertThat(batch.getSatzart200()).hasSize(2);
        assertThat(batch.getSatzart200().get(0).getFvBelnr()).isEqualTo("FV-1");
        // An accepted batch is not a failure, whatever the notification toggles say.
        verifyNoInteractions(this.mailOutPort);
    }

    @Test
    void exposesAnOpenApiDocumentGeneratedFromTheCanonicalContract() throws Exception {
        this.mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                // the REST batch operation is documented...
                .andExpect(jsonPath("$.paths['/api/pscd/batches'].post.responses.202").exists())
                // ...and its request schema is derived from the contract-generated canonical model
                .andExpect(jsonPath("$.components.schemas.PscdBatch.properties.satzart010").exists());
    }

    @Test
    void rejectsABatchMissingTheMandatoryFilenameWith400() throws Exception {
        this.mockMvc.perform(post(BATCHES).header(HttpHeaders.AUTHORIZATION, BASIC_AUTH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"satzart010\": { \"satzart\": \"010\", \"abstimmsumme\": \"1\", \"vorzeichen\": \"+\" } }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsABatchMissingTheMandatoryControlRecordWith400() throws Exception {
        this.mockMvc.perform(post(BATCHES).header(HttpHeaders.AUTHORIZATION, BASIC_AUTH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"filename\": \"bad.json\" }"))
                .andExpect(status().isBadRequest());
    }

    /**
     * End to end through the wiring: a rejected payload also mails the configured recipients, with the
     * body rendered from the template shipped with the service.
     */
    @Test
    void mailsTheConfiguredRecipientsAboutARejectedBatch() throws Exception {
        this.mockMvc.perform(post(BATCHES).header(HttpHeaders.AUTHORIZATION, BASIC_AUTH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"filename\": \"rejected.json\" }"))
                .andExpect(status().isBadRequest());

        final ArgumentCaptor<TextMail> captor = ArgumentCaptor.forClass(TextMail.class);
        verify(this.mailOutPort).sendTextMail(captor.capture());
        final TextMail mail = captor.getValue();
        assertThat(mail.getReceivers()).isEqualTo("ops@example.org");
        assertThat(mail.getSubject()).isEqualTo("PSCD batch processing failed");
        assertThat(mail.getBody())
                .contains("Channel:  REST")
                .contains("Batch:    rejected.json")
                // Nothing was kept on this channel: the caller still holds the payload.
                .contains("Location: -")
                .contains("missing the mandatory Satzart010 record");
    }
}
