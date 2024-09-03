package de.muenchen.refarch.integration.dms.application.usecase;

import de.muenchen.refarch.integration.dms.application.port.out.DepositObjectOutPort;
import de.muenchen.refarch.integration.dms.application.port.in.DepositObjectInPort;
import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
@RequiredArgsConstructor
public class DepositObjectUseCase implements DepositObjectInPort {

    private final DepositObjectOutPort depositObjectOutPort;

    @Override
    public void depositObject(@NotBlank String objectCoo, @NotBlank String user) throws DmsException {
        depositObjectOutPort.depositObject(objectCoo, user);
    }
}
