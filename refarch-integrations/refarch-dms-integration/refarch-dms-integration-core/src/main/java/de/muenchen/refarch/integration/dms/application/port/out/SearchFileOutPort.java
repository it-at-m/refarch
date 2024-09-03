package de.muenchen.refarch.integration.dms.application.port.out;

import java.util.List;

public interface SearchFileOutPort {

    List<String> searchFile(String searchString, String user, String reference, String value);
}
