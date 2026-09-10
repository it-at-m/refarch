package de.muenchen.oss.refarch.integration.pscd.example.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.muenchen.oss.refarch.integration.pscd.application.port.out.PscdOutPort;
import de.muenchen.oss.refarch.integration.pscd.domain.model.PscdSatzarten;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@TestPropertySource(properties = "refarch.pscd.client.url=http://localhost:1337/pscd-target")
class ExampleControllerTest {

    @Autowired
    private ExampleController exampleController;

    @MockitoBean
    private PscdOutPort pscdOutPort;

    @Test
    void buildsASampleBatchAndDeliversItToTheOutPort() {
        final ResponseEntity<Void> response = this.exampleController.submitSampleBatch("demo.dat");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        final ArgumentCaptor<PscdSatzarten> captor = ArgumentCaptor.forClass(PscdSatzarten.class);
        verify(this.pscdOutPort, times(1)).send(captor.capture());
        final PscdSatzarten batch = captor.getValue();
        assertThat(batch.getFilename()).isEqualTo("demo.dat");
        assertThat(batch.getSatzart010()).isNotNull();
        assertThat(batch.getSatzart200()).hasSize(1);
    }
}
