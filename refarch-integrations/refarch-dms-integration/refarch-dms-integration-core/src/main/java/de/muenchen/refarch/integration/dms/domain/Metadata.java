package de.muenchen.refarch.integration.dms.domain;

import lombok.Data;

/**
 * Represents the metadata of the object.
 */
@Data
public class Metadata {
  private final String name;
  private final String type;
  private final String url;

}
