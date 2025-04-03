package de.muenchen.refarch.integration.address.client.model.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListStreetsModel {

    private List<String> cityDistrictNames;

    private List<Long> cityDistrictNumbers;

    private String streetName;

    private String sortdir;

    private Integer page;

    private Integer pagesize;

}
