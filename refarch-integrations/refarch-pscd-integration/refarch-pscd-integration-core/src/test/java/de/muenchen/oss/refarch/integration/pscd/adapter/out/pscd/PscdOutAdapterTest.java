package de.muenchen.oss.refarch.integration.pscd.adapter.out.pscd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import de.lhm.pi.pscd.rahmenschnittstelle.DTSOAPSatzarten;
import de.muenchen.oss.refarch.integration.pscd.client.PscdSoapClient;
import de.muenchen.oss.refarch.integration.pscd.domain.exception.PscdProcessingException;
import de.muenchen.oss.refarch.integration.pscd.domain.model.PscdSatzarten;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart010;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart100;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PscdOutAdapterTest {

    private final PscdSoapClient pscdSoapClient = mock(PscdSoapClient.class);
    private final PscdOutAdapter adapter = new PscdOutAdapter(this.pscdSoapClient);

    @Test
    void mapsDomainBatchToRequestAndDelegatesToClient() {
        final PscdSatzarten batch = PscdSatzarten.builder()
                .filename("records.dat")
                .satzart010(Satzart010.builder().satzart("010").abstimmsumme("12345").vorzeichen("+").build())
                .satzart100(List.of(Satzart100.builder().satzart("100").psobkey("KEY1").partner("PARTNER1").build()))
                .build();

        this.adapter.send(batch);

        final ArgumentCaptor<DTSOAPSatzarten> captor = ArgumentCaptor.forClass(DTSOAPSatzarten.class);
        verify(this.pscdSoapClient).send(captor.capture());
        final DTSOAPSatzarten sent = captor.getValue();
        assertThat(sent.getFilename()).isEqualTo("records.dat");
        assertThat(sent.getSatzart010().getABSTIMMSUMME()).isEqualTo("12345");
        assertThat(sent.getSatzart100()).singleElement()
                .satisfies(record -> {
                    assertThat(record.getPSOBKEY()).isEqualTo("KEY1");
                    assertThat(record.getPARTNER()).isEqualTo("PARTNER1");
                });
    }

    @Test
    void wrapsClientRuntimeExceptionInProcessingException() {
        doThrow(new RuntimeException("connection refused")).when(this.pscdSoapClient).send(any());

        assertThatThrownBy(() -> this.adapter.send(PscdSatzarten.builder().filename("f").build()))
                .isInstanceOf(PscdProcessingException.class)
                .hasMessageContaining("Failed to call the PSCD SOAP endpoint");
    }
}
