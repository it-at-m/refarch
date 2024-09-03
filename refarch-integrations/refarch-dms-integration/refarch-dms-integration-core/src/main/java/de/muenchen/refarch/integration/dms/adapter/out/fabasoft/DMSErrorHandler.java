package de.muenchen.refarch.integration.dms.adapter.out.fabasoft;

import de.muenchen.refarch.integration.dms.domain.exception.DmsException;

public class DMSErrorHandler {
    public void handleError(int code, String errorMessage) throws DmsException {

        final DMSStatusCode statusCode = DMSStatusCode.byCode(code);

        if (statusCode != DMSStatusCode.UEBERTRAGUNG_ERFORLGREICH) {
            throw new DmsException(statusCode.toString(), errorMessage);
        }
    }
}
