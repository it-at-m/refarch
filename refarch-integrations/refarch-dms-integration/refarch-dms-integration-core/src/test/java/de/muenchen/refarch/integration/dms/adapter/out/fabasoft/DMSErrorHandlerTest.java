package de.muenchen.refarch.integration.dms.adapter.out.fabasoft;

import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DMSErrorHandlerTest {

    private DMSErrorHandler dmsErrorHandler;

    @BeforeEach
    void setup() {
        dmsErrorHandler = new DMSErrorHandler();
    }

    @Test
    void handleDmsExceptionUnknown() {

        int errorCode = -1;
        String statusCode = "UNBEKANNTER_FEHLER";
        String errorMessage = "Unbekannter Fehler";

        DmsException dmsException = assertThrows(DmsException.class, () -> this.dmsErrorHandler.handleError(errorCode, errorMessage));

        String actualMessage = dmsException.getMessage();

        assertEquals(statusCode + ": " + errorMessage, actualMessage);

        assertEquals(statusCode, dmsException.getStatusCode());
    }

    @Test
    void handleDmsException() {

        int errorCode = 2;
        String statusCode = "FEHLENDE_BERECHTIGUNG";
        String errorMessage = "Fehlende Berechtigung";

        DmsException dmsException = assertThrows(DmsException.class, () -> this.dmsErrorHandler.handleError(errorCode, errorMessage));

        String actualMessage = dmsException.getMessage();

        assertEquals(statusCode + ": " + errorMessage, actualMessage);

        assertEquals(statusCode, dmsException.getStatusCode());

    }

    @Test
    void uebertragungErfolgreich() throws DmsException {

        int code = 0;

        this.dmsErrorHandler.handleError(code, null);
    }
}