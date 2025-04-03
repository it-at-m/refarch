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
public class SearchAddressesGermanyModel {

    private String query;

    private String zip;

    private String cityName;

    private String gemeindeschluessel;

    private List<Long> houseNumberFilter;

    private List<String> letterFilter;

    private String sort;

    private String sortdir;

    private Integer page;

    private Integer pagesize;

}
