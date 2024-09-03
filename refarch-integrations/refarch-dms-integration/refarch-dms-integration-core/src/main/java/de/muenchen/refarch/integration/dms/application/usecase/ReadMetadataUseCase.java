package de.muenchen.refarch.integration.dms.application.usecase;

import de.muenchen.refarch.integration.dms.application.port.out.ReadMetadataOutPort;
import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Metadata;
import de.muenchen.refarch.integration.dms.domain.model.ObjectType;
import de.muenchen.refarch.integration.dms.application.port.in.ReadMetadataInPort;
import de.muenchen.refarch.integration.dms.application.port.out.DmsUserOutPort;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
@RequiredArgsConstructor
public class ReadMetadataUseCase implements ReadMetadataInPort {

    private final ReadMetadataOutPort readMetadataOutPort;

    private final DmsUserOutPort dmsUserOutPort;

    @Override
    public Metadata readMetadata(
            @NotNull final ObjectType objectclass,
            @NotBlank final String coo
    ) throws DmsException {

        String user = dmsUserOutPort.getDmsUser();

        if (objectclass == ObjectType.Schriftstueck) {
            return readMetadataOutPort.readContentMetadata(coo, user);
        }

        Metadata metadata = readMetadataOutPort.readMetadata(coo, user);

        String object = objectclass == ObjectType.Intern ?
                "Internes Dokument" :
                objectclass.toString();

        if (!object.equals(metadata.getType())) {
            throw new DmsException("WRONG_INPUT_OBJECT_CLASS", String.format("The input object with the COO address %s is invalid because it is of the object class %s and this does not match the expected object class(es) %s.", coo, metadata.getType(), objectclass));
        }
        return metadata;

    }

}
