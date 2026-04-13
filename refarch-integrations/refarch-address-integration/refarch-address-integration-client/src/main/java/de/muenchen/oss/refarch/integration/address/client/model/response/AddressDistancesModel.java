package de.muenchen.oss.refarch.integration.address.client.model.response;

import de.muenchen.oss.refarch.integration.address.client.gen.model.AdresseDistanz;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AddressDistancesModel {

    protected List<AdresseDistanz> addressDistances;

}
