package de.muenchen.oss.digiwf.address.integration.client.model.response;

import de.muenchen.refarch.integration.address.client.gen.model.AdresseDistanz;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AddressDistancesModel {

    List<AdresseDistanz> addressDistances;

}
