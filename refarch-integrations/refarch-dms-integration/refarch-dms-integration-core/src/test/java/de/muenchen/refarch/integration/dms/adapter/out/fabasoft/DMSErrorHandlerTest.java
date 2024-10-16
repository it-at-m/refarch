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

        final int errorCode = -1;
        final String statusCode = "UNBEKANNTER_FEHLER";
        final String errorMessage = "Unbekannter Fehler";

        final DmsException dmsException = assertThrows(DmsException.class, () -> this.dmsErrorHandler.handleError(errorCode, errorMessage));

        final String actualMessage = dmsException.getMessage();

        assertEquals(statusCode + ": " + errorMessage, actualMessage);

        assertEquals(statusCode, dmsException.getStatusCode());
    }

    @Test
    void handleDmsException() {

        final int errorCode = 2;
        final String statusCode = "FEHLENDE_BERECHTIGUNG";
        final String errorMessage = "Fehlende Berechtigung";

        final DmsException dmsException = assertThrows(DmsException.class, () -> this.dmsErrorHandler.handleError(errorCode, errorMessage));

        final String actualMessage = dmsException.getMessage();

        assertEquals(statusCode + ": " + errorMessage, actualMessage);

        assertEquals(statusCode, dmsException.getStatusCode());

    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void uebertragungErfolgreich() throws DmsException {

        final int code = 0;

        this.dmsErrorHandler.handleError(code, null);
    }
}
