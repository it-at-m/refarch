package de.muenchen.refarch.integration.dms.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Content {
    private String extension;
    private String name;
    private byte[] content;

}
